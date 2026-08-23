# Rawalworld — Google Play Release Pack

## App identity
- App name: Rawalworld
- Android application ID: `com.astrolife.app`
- Current version: 1.1.0
- Current version code: 2
- Minimum Android: 7.0 (API 24)
- Target Android SDK: 35

> Keep the existing application ID if you want future APK/AAB releases to update the currently installed Rawalworld app instead of becoming a separate app.

## Suggested Play Store listing

### Short description
Astrology, travel, shopping, bookings, Gujarat updates and everyday services in one app.

### Full description
Rawalworld brings useful lifestyle and service features together in one simple app for users in Gujarat and beyond.

Explore astrology tools and live daily information, Hindu and Gujarati calendar resources, daily Rashi, event and decoration services, catering and consultancy, tours and travel, maps, Gujarat news, weather updates, entertainment, live cricket links, games and multilingual music links.

Rawalworld also includes category-wise online shopping with product photos, INR pricing, quantity selection, delivery details and UPI / Google Pay checkout. Users can submit service bookings and quotation requests and maintain a basic profile.

The secure Admin area supports Payment Master settings, product creation and management, master creation and management, gallery photos, service contact details, client records, bookings and order management.

Available languages include English, Gujarati, Hindi and French.

### Suggested category
Lifestyle

### Contact details
- Support email: rawalworld@gmail.com
- Support phone: +91 77093 78969
- Service region: Gujarat, India

## Data Safety working notes
Rawalworld may process information entered by users for bookings, shopping and service requests, including name, mobile number, city, delivery address, pincode, preferred date and service requirements. Admin authentication uses Supabase authentication. Product/order and booking records are stored in the connected Supabase backend.

The app opens third-party websites or apps for selected services such as maps, travel search, weather, news, astrology resources, entertainment and UPI payment. Rawalworld does not itself process bank credentials; payment is handed to the user's installed UPI-capable app through an Android payment intent.

Before Play Store submission, confirm the final Google Play Data Safety answers against the production database policies and any analytics/SDKs added later.

## Store assets still required
- 512 × 512 Play Store app icon
- 1024 × 500 feature graphic
- At least 2 phone screenshots; 4–8 recommended
- Privacy Policy public URL
- App access instructions for the Admin area if Google review requires access

## Release signing
Google Play requires a signed release Android App Bundle (AAB). The repository workflow `Build Rawalworld Play AAB` expects these GitHub Actions secrets:

- `RAWALWORLD_KEYSTORE_B64` — Base64-encoded release keystore
- `RAWALWORLD_STORE_PASSWORD` — keystore password
- `RAWALWORLD_KEY_ALIAS` — signing key alias
- `RAWALWORLD_KEY_PASSWORD` — signing key password

Do not commit a keystore or passwords to the repository.

## Final pre-launch checklist
1. Install and test the latest final APK on at least one Android 14/15/16 device.
2. Verify all language options and navigation.
3. Verify online links open correctly.
4. Verify Admin login and Forgot Password.
5. Verify Payment Master changes reflect in checkout.
6. Verify product photo upload, category display and order flow.
7. Verify Master create/edit/delete and client records.
8. Verify gallery upload/display and service contacts.
9. Publish the privacy policy at a public HTTPS URL.
10. Add release-signing secrets in GitHub.
11. Run `Build Rawalworld Play AAB` and upload the generated `.aab` to Play Console Internal Testing first.
12. Complete Content Rating, App Access, Ads declaration and Data Safety in Play Console.
13. Test Internal Testing build before Production rollout.
