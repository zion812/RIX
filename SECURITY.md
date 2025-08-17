# Security Policy

## Secret Management

### Critical Security Incident: Keystore Exposure

**Status**: CRITICAL - Immediate action required

**Issue**: The file `rio-upload-key.keystore` was committed to version control, exposing signing credentials.

**Impact**: 
- Signing keys are compromised
- Potential for unauthorized app releases
- Security vulnerability for production deployment

**Immediate Actions Required**:

1. **Remove keystore from repository**:
   ```bash
   git rm --cached rio-upload-key.keystore
   echo "rio-upload-key.keystore" >> .gitignore
   git add .gitignore
   git commit -m "Remove exposed keystore and add to .gitignore"
   ```

2. **Rotate signing keys**:
   - Generate new keystore with different passwords
   - Update Google Play Console with new upload key
   - Update CI/CD pipelines with new credentials

3. **Purge from Git history** (if sensitive):
   ```bash
   # Using git filter-repo (recommended)
   git filter-repo --path rio-upload-key.keystore --invert-paths
   
   # Or using BFG Repo-Cleaner
   java -jar bfg.jar --delete-files rio-upload-key.keystore
   ```

### Security Best Practices

#### Secrets Management
- **Never commit** keystores, API keys, or credentials to VCS
- Use environment variables or secure CI secret stores
- Keep `google-services.json` out of public repositories
- Use different configurations for dev/staging/production

#### Recommended File Structure
```
# Local files (not committed)
local.properties          # SDK paths, signing configs
keystore/                 # All keystores
  debug.keystore
  release.keystore
  upload.keystore
google-services/          # Firebase configs per environment
  dev/google-services.json
  prod/google-services.json

# Committed files
.gitignore                # Excludes above files
gradle.properties         # Non-sensitive build configs
```

#### Gradle Configuration
```kotlin
// app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            // Read from local.properties or environment
            storeFile = file(project.findProperty("RELEASE_STORE_FILE") ?: "")
            storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String?
            keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as String?
            keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String?
        }
    }
}
```

#### local.properties Example
```properties
# Never commit this file
RELEASE_STORE_FILE=../keystore/release.keystore
RELEASE_STORE_PASSWORD=your_store_password
RELEASE_KEY_ALIAS=your_key_alias
RELEASE_KEY_PASSWORD=your_key_password
```

### Firebase Security

#### google-services.json
- Use different Firebase projects for dev/staging/production
- Restrict API keys to specific package names and SHA fingerprints
- Enable App Check for production

#### Firestore Security Rules
- Validate all user inputs server-side
- Use custom claims for authorization, not client-side checks
- Implement rate limiting and abuse prevention

### CI/CD Security

#### GitHub Actions / CI Secrets
```yaml
# Store as repository secrets
RELEASE_KEYSTORE_BASE64    # Base64 encoded keystore
RELEASE_STORE_PASSWORD
RELEASE_KEY_ALIAS
RELEASE_KEY_PASSWORD
GOOGLE_SERVICES_JSON       # Base64 encoded config
```

#### Build Script Security
```bash
# Decode secrets in CI
echo $RELEASE_KEYSTORE_BASE64 | base64 -d > app/release.keystore
echo $GOOGLE_SERVICES_JSON | base64 -d > app/google-services.json
```

### Incident Response

#### If Credentials Are Compromised
1. **Immediate**: Revoke/rotate all affected credentials
2. **Assessment**: Determine scope of exposure and potential impact
3. **Notification**: Inform relevant stakeholders
4. **Documentation**: Record incident and lessons learned
5. **Prevention**: Implement additional safeguards

#### Monitoring
- Set up alerts for unauthorized access attempts
- Monitor Firebase usage for anomalies
- Regular security audits of dependencies
- Automated vulnerability scanning

### Contact

For security issues:
- **Critical**: Immediate Slack/phone notification
- **Non-critical**: security@rostry-platform.com
- **Vulnerability reports**: Follow responsible disclosure

---

**Last Updated**: December 2024  
**Next Review**: Quarterly security audit  
**Incident Status**: ACTIVE - Keystore rotation required

## Current Security Implementation

**Implemented Security Measures:**
- ✅ Firebase authentication with email/password and phone verification
- ✅ Firebase security rules for Firestore and Storage
- ✅ Data encryption in transit (HTTPS/Firebase)
- ⚠️ Debug signing configuration still present in build files (needs removal)

## Security Recommendations

1. Remove debug signing configuration from build.gradle files before production deployment
2. Implement biometric authentication for sensitive operations
3. Add certificate pinning for network requests
4. Implement secure storage for sensitive data
