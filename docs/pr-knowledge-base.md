# RIO PR/Product Knowledge Base (Business-Friendly)

Audience: PR, Product, Partnerships, and Field Teams
Last updated: 2025-08-11

## 1) Product Overview
- RIO (Rural Information eXchange) is a rooster/fowl community and marketplace app built for rural India, especially Andhra Pradesh and Telangana.
- Core value: digitize breeding records, enable trusted transfers/sales, and support farmer livelihoods with analytics and advisory.

## 2) Key Capabilities
- Fowl Registry: register roosters/hens with photos, lineage, and health notes
- Family Tree: visualize pedigree and breeding outcomes
- Marketplace: list, discover, and safely transfer ownership
- Messaging: connect buyers and sellers; plan transfers
- Tiered Access: General, Farmer, Enthusiast tiers unlock features progressively
- Offline-First: works smoothly with intermittent 2G/3G; syncs when online
- Multi-language: Telugu, Hindi, English (UX prepared; content rollout phased)

## 3) Personas & Target Users
- Smallholder Farmer (primary): rural poultry raiser, low-cost Android device, intermittent connectivity
- Enthusiast/Breeder: hobbyist focused on quality breeds and pedigree tracking
- Local Agent/KVK Coordinator: supports onboarding and verifies transfers

## 4) Value Propositions
- Trusted Identity: verified transfers and lineage reduce fraud
- Income Uplift: direct market access, discovery beyond the village
- Simplicity for Rural Use: large touch targets, local language, works offline
- Insights: simple analytics on breeding and sales (phase-by-phase rollout)

## 5) Differentiators
- Rural-first engineering: offline cache, network-aware behaviors, low device overhead
- Tiered onboarding aligned to farmer journey (General → Farmer → Enthusiast)
- Community safety: structured verification flows and notifications
- Regional partnerships with KVKs and agri networks

## 6) Technical Specs (Plain English)
- Mobile App: Android, modern UI, optimized for low bandwidth
- Backend: Google Firebase for login, data, and server code
- Security: user tiers and permissions enforce who can do what
- Data: two copies—on the phone for offline use, and in the cloud for backup and syncing

## 7) Integrations and Partnerships
- Payments: Razorpay and UPI design in place for coin-based economy (₹5 per coin). Production keys to be provisioned per partner timeline.
- Government & KVKs: built-in verification workflows; supports district and region metadata for analytics and outreach
- Messaging/Notifications: Firebase push notifications (enablement pending module activation)

## 8) Rural-Focused Launch Plan (Highlights)
- Onboarding: in-person drives with KVK partners; QR flyers; WhatsApp community
- Language: Telugu-first screens; visual cues and voice prompts roadmap
- Connectivity: pre-packaged content; background sync; minimal data per screen
- Success Metrics (first 6 months):
  - 1,000+ farmers onboarded
  - 70% retention (active at month 2)
  - 5,000+ fowls registered
  - <1% crash rate; <3s app start on low-end devices

## 9) FAQs
- Does it work offline? Yes. You can register fowls and view cached data; it syncs later.
- Is data safe? Yes. Accounts use secure Google login and tier-based permissions.
- Can we integrate other wallets? UPI-first; others can be added via backend functions.
- What phones are supported? Most Android phones from Android 7.0 (API 24) upwards.

## 10) Contacts
- Technical Lead: tech@rio-platform.com
- Partnerships: partnerships@rio-platform.com
- Field Support: support@rio-platform.com

