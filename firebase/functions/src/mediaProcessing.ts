import * as functions from 'firebase-functions';
import * as admin from 'firebase-admin';
import { Storage } from '@google-cloud/storage';
import * as sharp from 'sharp';
import * as ffmpeg from 'fluent-ffmpeg';
import * as path from 'path';
import * as os from 'os';
import * as fs from 'fs';

// Initialize Firebase Admin
admin.initializeApp();
const storage = new Storage();
const bucket = storage.bucket('rio-storage-bucket');

// Regional configuration for India
const REGION = 'asia-south1';

/**
 * Image processing function triggered on upload
 * Generates multiple quality versions optimized for rural networks
 */
export const processImage = functions
  .region(REGION)
  .storage
  .object()
  .onFinalize(async (object) => {
    const filePath = object.name!;
    const contentType = object.contentType;
    
    // Only process images
    if (!contentType || !contentType.startsWith('image/')) {
      console.log('Not an image, skipping processing');
      return null;
    }
    
    // Skip if already processed (avoid infinite loops)
    if (filePath.includes('_processed_') || 
        filePath.includes('_thumbnail') || 
        filePath.includes('_compressed')) {
      console.log('Already processed, skipping');
      return null;
    }
    
    // Skip system files
    if (filePath.startsWith('system/') || filePath.startsWith('temp/')) {
      console.log('System file, skipping processing');
      return null;
    }
    
    try {
      const fileName = path.basename(filePath);
      const fileDir = path.dirname(filePath);
      const fileExtension = path.extname(fileName);
      const fileNameWithoutExt = path.basename(fileName, fileExtension);
      
      // Download original file
      const tempFilePath = path.join(os.tmpdir(), fileName);
      await bucket.file(filePath).download({ destination: tempFilePath });
      
      console.log(`Processing image: ${filePath}`);
      
      // Determine processing strategy based on file path
      const processingConfig = getProcessingConfig(filePath);
      
      // Generate different quality versions
      const processedFiles = await Promise.all([
        // High quality (720p equivalent)
        generateImageVariant(tempFilePath, fileDir, fileNameWithoutExt, 'high', {
          width: 1280,
          height: 720,
          quality: 85,
          format: 'jpeg'
        }),
        
        // Medium quality (480p equivalent)
        generateImageVariant(tempFilePath, fileDir, fileNameWithoutExt, 'medium', {
          width: 854,
          height: 480,
          quality: 70,
          format: 'jpeg'
        }),
        
        // Low quality (240p equivalent)
        generateImageVariant(tempFilePath, fileDir, fileNameWithoutExt, 'low', {
          width: 640,
          height: 360,
          quality: 50,
          format: 'jpeg'
        }),
        
        // Thumbnail
        generateImageVariant(tempFilePath, fileDir, fileNameWithoutExt, 'thumbnail', {
          width: 150,
          height: 150,
          quality: 60,
          format: 'jpeg',
          fit: 'cover'
        }),
        
        // WebP versions for modern browsers
        generateImageVariant(tempFilePath, fileDir, fileNameWithoutExt, 'high', {
          width: 1280,
          height: 720,
          quality: 80,
          format: 'webp'
        }),
        
        generateImageVariant(tempFilePath, fileDir, fileNameWithoutExt, 'medium', {
          width: 854,
          height: 480,
          quality: 65,
          format: 'webp'
        })
      ]);
      
      // Update metadata in Firestore
      await updateMediaMetadata(filePath, processedFiles, processingConfig);
      
      // Clean up temp file
      fs.unlinkSync(tempFilePath);
      
      console.log(`Successfully processed image: ${filePath}`);
      return null;
      
    } catch (error) {
      console.error('Error processing image:', error);
      
      // Log error to Firestore for monitoring
      await admin.firestore().collection('processing_errors').add({
        filePath,
        error: error.message,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        type: 'image_processing'
      });
      
      return null;
    }
  });

/**
 * Video processing function for rural network optimization
 */
export const processVideo = functions
  .region(REGION)
  .runWith({
    timeoutSeconds: 540, // 9 minutes
    memory: '2GB'
  })
  .storage
  .object()
  .onFinalize(async (object) => {
    const filePath = object.name!;
    const contentType = object.contentType;
    
    // Only process videos
    if (!contentType || !contentType.startsWith('video/')) {
      return null;
    }
    
    // Skip if already processed
    if (filePath.includes('_processed_') || filePath.includes('_compressed')) {
      return null;
    }
    
    try {
      const fileName = path.basename(filePath);
      const fileDir = path.dirname(filePath);
      const fileNameWithoutExt = path.basename(fileName, path.extname(fileName));
      
      // Download original file
      const tempFilePath = path.join(os.tmpdir(), fileName);
      await bucket.file(filePath).download({ destination: tempFilePath });
      
      console.log(`Processing video: ${filePath}`);
      
      // Generate different quality versions for rural networks
      const processedFiles = await Promise.all([
        // 720p for WiFi/4G
        generateVideoVariant(tempFilePath, fileDir, fileNameWithoutExt, '720p', {
          resolution: '1280x720',
          bitrate: '2000k',
          fps: 30
        }),
        
        // 480p for 3G
        generateVideoVariant(tempFilePath, fileDir, fileNameWithoutExt, '480p', {
          resolution: '854x480',
          bitrate: '1000k',
          fps: 24
        }),
        
        // 240p for 2G
        generateVideoVariant(tempFilePath, fileDir, fileNameWithoutExt, '240p', {
          resolution: '426x240',
          bitrate: '500k',
          fps: 15
        }),
        
        // Generate thumbnail
        generateVideoThumbnail(tempFilePath, fileDir, fileNameWithoutExt)
      ]);
      
      // Update metadata
      await updateVideoMetadata(filePath, processedFiles);
      
      // Clean up
      fs.unlinkSync(tempFilePath);
      
      console.log(`Successfully processed video: ${filePath}`);
      return null;
      
    } catch (error) {
      console.error('Error processing video:', error);
      
      await admin.firestore().collection('processing_errors').add({
        filePath,
        error: error.message,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        type: 'video_processing'
      });
      
      return null;
    }
  });

/**
 * Generate image variant with specified parameters
 */
async function generateImageVariant(
  inputPath: string,
  outputDir: string,
  baseName: string,
  quality: string,
  options: {
    width: number;
    height: number;
    quality: number;
    format: 'jpeg' | 'webp';
    fit?: 'cover' | 'contain' | 'fill';
  }
): Promise<string> {
  const outputFileName = `${baseName}_${quality}.${options.format}`;
  const outputPath = `${outputDir}/${outputFileName}`;
  const tempOutputPath = path.join(os.tmpdir(), outputFileName);
  
  let sharpInstance = sharp(inputPath)
    .resize(options.width, options.height, { 
      fit: options.fit || 'inside',
      withoutEnlargement: true 
    });
  
  if (options.format === 'jpeg') {
    sharpInstance = sharpInstance.jpeg({ 
      quality: options.quality,
      progressive: true // Progressive JPEG for better loading on slow connections
    });
  } else if (options.format === 'webp') {
    sharpInstance = sharpInstance.webp({ 
      quality: options.quality,
      effort: 6 // Higher compression effort
    });
  }
  
  await sharpInstance.toFile(tempOutputPath);
  
  // Upload to Firebase Storage
  await bucket.upload(tempOutputPath, {
    destination: outputPath,
    metadata: {
      contentType: `image/${options.format}`,
      cacheControl: 'public, max-age=31536000', // 1 year cache
      metadata: {
        originalFile: path.basename(inputPath),
        quality: quality,
        processedAt: new Date().toISOString(),
        optimizedForRural: 'true'
      }
    }
  });
  
  // Clean up temp file
  fs.unlinkSync(tempOutputPath);
  
  return outputPath;
}

/**
 * Generate video variant with specified parameters
 */
async function generateVideoVariant(
  inputPath: string,
  outputDir: string,
  baseName: string,
  quality: string,
  options: {
    resolution: string;
    bitrate: string;
    fps: number;
  }
): Promise<string> {
  return new Promise((resolve, reject) => {
    const outputFileName = `${baseName}_${quality}.mp4`;
    const outputPath = `${outputDir}/${outputFileName}`;
    const tempOutputPath = path.join(os.tmpdir(), outputFileName);
    
    ffmpeg(inputPath)
      .videoCodec('libx264')
      .audioCodec('aac')
      .size(options.resolution)
      .videoBitrate(options.bitrate)
      .fps(options.fps)
      .audioFrequency(44100)
      .audioChannels(2)
      .audioBitrate('128k')
      .format('mp4')
      .outputOptions([
        '-preset fast',
        '-crf 23',
        '-movflags +faststart', // Optimize for streaming
        '-profile:v baseline', // Better compatibility
        '-level 3.0'
      ])
      .on('end', async () => {
        try {
          // Upload to Firebase Storage
          await bucket.upload(tempOutputPath, {
            destination: outputPath,
            metadata: {
              contentType: 'video/mp4',
              cacheControl: 'public, max-age=31536000',
              metadata: {
                originalFile: path.basename(inputPath),
                quality: quality,
                resolution: options.resolution,
                bitrate: options.bitrate,
                processedAt: new Date().toISOString(),
                optimizedForRural: 'true'
              }
            }
          });
          
          // Clean up
          fs.unlinkSync(tempOutputPath);
          resolve(outputPath);
        } catch (error) {
          reject(error);
        }
      })
      .on('error', reject)
      .save(tempOutputPath);
  });
}

/**
 * Generate video thumbnail
 */
async function generateVideoThumbnail(
  inputPath: string,
  outputDir: string,
  baseName: string
): Promise<string> {
  return new Promise((resolve, reject) => {
    const outputFileName = `${baseName}_thumbnail.jpg`;
    const outputPath = `${outputDir}/${outputFileName}`;
    const tempOutputPath = path.join(os.tmpdir(), outputFileName);
    
    ffmpeg(inputPath)
      .screenshots({
        timestamps: ['10%'], // Take screenshot at 10% of video duration
        filename: outputFileName,
        folder: os.tmpdir(),
        size: '320x240'
      })
      .on('end', async () => {
        try {
          await bucket.upload(tempOutputPath, {
            destination: outputPath,
            metadata: {
              contentType: 'image/jpeg',
              cacheControl: 'public, max-age=31536000',
              metadata: {
                originalFile: path.basename(inputPath),
                type: 'video_thumbnail',
                processedAt: new Date().toISOString()
              }
            }
          });
          
          fs.unlinkSync(tempOutputPath);
          resolve(outputPath);
        } catch (error) {
          reject(error);
        }
      })
      .on('error', reject);
  });
}

/**
 * Get processing configuration based on file path
 */
function getProcessingConfig(filePath: string): any {
  if (filePath.includes('/transfers/')) {
    return {
      priority: 'critical',
      retainOriginal: true,
      generateWebP: true,
      compressionLevel: 'medium'
    };
  } else if (filePath.includes('/marketplace/')) {
    return {
      priority: 'high',
      retainOriginal: false,
      generateWebP: true,
      compressionLevel: 'high'
    };
  } else {
    return {
      priority: 'normal',
      retainOriginal: false,
      generateWebP: false,
      compressionLevel: 'high'
    };
  }
}

/**
 * Update media metadata in Firestore
 */
async function updateMediaMetadata(filePath: string, processedFiles: string[], config: any): Promise<void> {
  const pathParts = filePath.split('/');
  if (pathParts.length < 4) return;
  
  const userId = pathParts[1];
  const fowlId = pathParts[3];
  
  await admin.firestore()
    .collection('media_metadata')
    .doc(path.basename(filePath))
    .set({
      originalPath: filePath,
      processedFiles: processedFiles,
      userId: userId,
      fowlId: fowlId,
      processingConfig: config,
      processedAt: admin.firestore.FieldValue.serverTimestamp(),
      status: 'completed'
    });
}

/**
 * Update video metadata in Firestore
 */
async function updateVideoMetadata(filePath: string, processedFiles: string[]): Promise<void> {
  const pathParts = filePath.split('/');
  if (pathParts.length < 4) return;
  
  const userId = pathParts[1];
  const fowlId = pathParts[3];
  
  await admin.firestore()
    .collection('video_metadata')
    .doc(path.basename(filePath))
    .set({
      originalPath: filePath,
      processedFiles: processedFiles,
      userId: userId,
      fowlId: fowlId,
      processedAt: admin.firestore.FieldValue.serverTimestamp(),
      status: 'completed'
    });
}
