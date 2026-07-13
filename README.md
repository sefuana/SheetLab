# SheetLab

**Created by axSaaFe**

A powerful mobile XLSX editor with floating clipboard assistant.

## Features
- Animated splash screen
- Secure login (Username: Sheetlabpro / Password: axSaaFe-admin)
- Dark/Light mode toggle
- Search XLSX files instantly
- Create, edit, rename, download, delete XLSX files
- Full spreadsheet editor (A-Z columns, 50+ rows)
- Easy To Fetch — floating overlay button
  - Tap `+` to show A/B/C buttons
  - Copy text → tap A/B/C to paste into that column in your sheet
  - Drag `+` anywhere on screen
  - Stop via notification

## Build from Termux

```bash
# Clone your repo
cd ~
git clone https://github.com/YOUR_USERNAME/SheetLab.git
cd SheetLab

# Push to GitHub — Actions will build APK automatically
git add .
git commit -m "Initial commit"
git push origin main
```

Then go to **GitHub → Actions → Build SheetLab APK → Artifacts** to download APK.

## Login Credentials
- Username: `Sheetlabpro`
- Password: `axSaaFe-admin`

## Permissions Required
- Overlay (for Easy To Fetch floating button)
- Storage (for file download to Downloads folder)
- Foreground Service (notification for Easy To Fetch)
