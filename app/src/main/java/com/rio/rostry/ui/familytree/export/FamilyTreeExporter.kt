package com.rio.rostry.ui.familytree.export

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.rio.rostry.core.database.entities.RoosterEntity
import com.rio.rostry.ui.familytree.model.FowlNode
import com.rio.rostry.ui.familytree.model.TreeConnection
import com.rio.rostry.ui.familytree.renderer.TreeRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Handles exporting family tree visualizations to various formats
 * Supports PDF, PNG, and data export formats
 */
class FamilyTreeExporter(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    
    /**
     * Export family tree as high-resolution PNG image
     */
    suspend fun exportAsPNG(
        rootFowl: RoosterEntity,
        nodes: List<FowlNode>,
        connections: List<TreeConnection>,
        width: Int = 2048,
        height: Int = 2048
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val bitmap = createTreeBitmap(nodes, connections, width, height)
            val file = createExportFile("${rootFowl.name}_family_tree", "png")
            
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            bitmap.recycle()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            
        } catch (e: Exception) {
            android.util.Log.e("FamilyTreeExporter", "Failed to export PNG", e)
            null
        }
    }
    
    /**
     * Export family tree as PDF document
     */
    suspend fun exportAsPDF(
        rootFowl: RoosterEntity,
        nodes: List<FowlNode>,
        connections: List<TreeConnection>,
        includeDetails: Boolean = true
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            
            // Create main tree page
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            // Draw tree
            drawTreeOnCanvas(canvas, nodes, connections, pageInfo.pageWidth, pageInfo.pageHeight)
            
            pdfDocument.finishPage(page)
            
            // Add details page if requested
            if (includeDetails) {
                addDetailsPage(pdfDocument, rootFowl, nodes)
            }
            
            // Save PDF
            val file = createExportFile("${rootFowl.name}_family_tree", "pdf")
            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
            }
            
            pdfDocument.close()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            
        } catch (e: Exception) {
            android.util.Log.e("FamilyTreeExporter", "Failed to export PDF", e)
            null
        }
    }
    
    /**
     * Export family tree data as JSON
     */
    suspend fun exportAsJSON(
        rootFowl: RoosterEntity,
        nodes: List<FowlNode>,
        connections: List<TreeConnection>
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val exportData = FamilyTreeExportData(
                rootFowl = rootFowl,
                fowlMembers = nodes.map { it.fowl },
                relationships = connections.map { connection ->
                    RelationshipData(
                        parentId = connection.parent.id,
                        childId = connection.child.id,
                        type = connection.type.name,
                        isVerified = connection.isVerified
                    )
                },
                exportDate = Date(),
                exportVersion = "1.0"
            )
            
            val json = com.google.gson.Gson().toJson(exportData)
            val file = createExportFile("${rootFowl.name}_family_data", "json")
            
            file.writeText(json)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            
        } catch (e: Exception) {
            android.util.Log.e("FamilyTreeExporter", "Failed to export JSON", e)
            null
        }
    }
    
    /**
     * Share family tree via system share dialog
     */
    suspend fun shareTree(
        rootFowl: RoosterEntity,
        nodes: List<FowlNode>,
        connections: List<TreeConnection>,
        format: ExportFormat = ExportFormat.PNG
    ) {
        val uri = when (format) {
            ExportFormat.PNG -> exportAsPNG(rootFowl, nodes, connections)
            ExportFormat.PDF -> exportAsPDF(rootFowl, nodes, connections)
            ExportFormat.JSON -> exportAsJSON(rootFowl, nodes, connections)
        }
        
        uri?.let { shareUri ->
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = when (format) {
                    ExportFormat.PNG -> "image/png"
                    ExportFormat.PDF -> "application/pdf"
                    ExportFormat.JSON -> "application/json"
                }
                putExtra(Intent.EXTRA_STREAM, shareUri)
                putExtra(Intent.EXTRA_SUBJECT, "${rootFowl.name} Family Tree")
                putExtra(Intent.EXTRA_TEXT, "Family tree for ${rootFowl.name} exported from RIO")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val shareIntent = Intent.createChooser(intent, "Share Family Tree")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        }
    }
    
    /**
     * Create print-friendly version of family tree
     */
    suspend fun createPrintVersion(
        rootFowl: RoosterEntity,
        nodes: List<FowlNode>,
        connections: List<TreeConnection>
    ): Bitmap = withContext(Dispatchers.Default) {
        // Create high-resolution bitmap optimized for printing
        val printWidth = 2480 // 300 DPI for 8.27 inches (A4 width)
        val printHeight = 3508 // 300 DPI for 11.69 inches (A4 height)
        
        createTreeBitmap(nodes, connections, printWidth, printHeight, true)
    }
    
    private fun createTreeBitmap(
        nodes: List<FowlNode>,
        connections: List<TreeConnection>,
        width: Int,
        height: Int,
        printMode: Boolean = false
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // White background for export
        canvas.drawColor(Color.WHITE)
        
        // Create renderer for export
        val renderer = TreeRenderer(context)
        renderer.initialize()
        
        // Calculate scale to fit all nodes
        val treeBounds = calculateTreeBounds(nodes)
        val scaleX = (width * 0.9f) / treeBounds.width()
        val scaleY = (height * 0.9f) / treeBounds.height()
        val scale = minOf(scaleX, scaleY, 1.0f)
        
        // Center the tree
        val offsetX = (width - treeBounds.width() * scale) / 2f - treeBounds.left * scale
        val offsetY = (height - treeBounds.height() * scale) / 2f - treeBounds.top * scale
        
        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scale, scale)
        
        // Draw tree components
        val visibleBounds = RectF(0f, 0f, width / scale, height / scale)
        renderer.drawConnections(canvas, connections, visibleBounds, scale)
        renderer.drawNodes(canvas, nodes, visibleBounds, scale)
        
        canvas.restore()
        
        // Add header and footer for print mode
        if (printMode) {
            addPrintHeader(canvas, nodes.firstOrNull()?.fowl?.name ?: "Family Tree", width)
            addPrintFooter(canvas, width, height)
        }
        
        return bitmap
    }
    
    private fun drawTreeOnCanvas(
        canvas: Canvas,
        nodes: List<FowlNode>,
        connections: List<TreeConnection>,
        pageWidth: Int,
        pageHeight: Int
    ) {
        // Similar to createTreeBitmap but optimized for PDF
        val renderer = TreeRenderer(context)
        renderer.initialize()
        
        val treeBounds = calculateTreeBounds(nodes)
        val scaleX = (pageWidth * 0.8f) / treeBounds.width()
        val scaleY = (pageHeight * 0.8f) / treeBounds.height()
        val scale = minOf(scaleX, scaleY, 1.0f)
        
        val offsetX = (pageWidth - treeBounds.width() * scale) / 2f - treeBounds.left * scale
        val offsetY = (pageHeight - treeBounds.height() * scale) / 2f - treeBounds.top * scale + 50f
        
        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scale, scale)
        
        val visibleBounds = RectF(0f, 0f, pageWidth / scale, pageHeight / scale)
        renderer.drawConnections(canvas, connections, visibleBounds, scale)
        renderer.drawNodes(canvas, nodes, visibleBounds, scale)
        
        canvas.restore()
    }
    
    private fun addDetailsPage(
        pdfDocument: PdfDocument,
        rootFowl: RoosterEntity,
        nodes: List<FowlNode>
    ) {
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }
        
        var y = 50f
        
        // Title
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("Family Tree Details", 50f, y, paint)
        y += 30f
        
        paint.textSize = 12f
        paint.isFakeBoldText = false
        
        // Root fowl details
        canvas.drawText("Root: ${rootFowl.name}", 50f, y, paint)
        y += 20f
        canvas.drawText("Breed: ${rootFowl.breed}", 50f, y, paint)
        y += 20f
        canvas.drawText("Gender: ${rootFowl.gender}", 50f, y, paint)
        y += 30f
        
        // Family members list
        canvas.drawText("Family Members:", 50f, y, paint)
        y += 20f
        
        nodes.sortedBy { it.generation }.forEach { node ->
            if (y > pageInfo.pageHeight - 50) return@forEach // Prevent overflow
            
            val fowl = node.fowl
            val text = "Gen ${node.generation}: ${fowl.name} (${fowl.breed}, ${fowl.gender})"
            canvas.drawText(text, 70f, y, paint)
            y += 15f
        }
        
        pdfDocument.finishPage(page)
    }
    
    private fun addPrintHeader(canvas: Canvas, title: String, width: Int) {
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }
        
        canvas.drawText(title, width / 2f, 40f, paint)
        
        // Draw line under title
        paint.strokeWidth = 2f
        canvas.drawLine(50f, 60f, width - 50f, 60f, paint)
    }
    
    private fun addPrintFooter(canvas: Canvas, width: Int, height: Int) {
        val paint = Paint().apply {
            color = Color.GRAY
            textSize = 12f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        
        val footerText = "Generated by RIO - ${dateFormat.format(Date())}"
        canvas.drawText(footerText, width / 2f, height - 20f, paint)
    }
    
    private fun calculateTreeBounds(nodes: List<FowlNode>): RectF {
        if (nodes.isEmpty()) return RectF()
        
        var minX = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var minY = Float.MAX_VALUE
        var maxY = Float.MIN_VALUE
        
        nodes.forEach { node ->
            minX = minOf(minX, node.x - node.radius)
            maxX = maxOf(maxX, node.x + node.radius)
            minY = minOf(minY, node.y - node.radius)
            maxY = maxOf(maxY, node.y + node.radius)
        }
        
        return RectF(minX, minY, maxX, maxY)
    }
    
    private fun createExportFile(baseName: String, extension: String): File {
        val timestamp = dateFormat.format(Date())
        val fileName = "${baseName}_$timestamp.$extension"
        
        val exportsDir = File(context.getExternalFilesDir(null), "exports")
        if (!exportsDir.exists()) {
            exportsDir.mkdirs()
        }
        
        return File(exportsDir, fileName)
    }
}

/**
 * Export format enumeration
 */
enum class ExportFormat {
    PNG, PDF, JSON
}

/**
 * Family tree export data structure
 */
data class FamilyTreeExportData(
    val rootFowl: RoosterEntity,
    val fowlMembers: List<RoosterEntity>,
    val relationships: List<RelationshipData>,
    val exportDate: Date,
    val exportVersion: String
)

/**
 * Relationship data for export
 */
data class RelationshipData(
    val parentId: String,
    val childId: String,
    val type: String,
    val isVerified: Boolean
)
