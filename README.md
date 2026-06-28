# AlphanixYoutube

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-brightgreen)](https://developer.android.com/)

**Alphanix Youtube** is a privacy‑focused Android client for YouTube that **does not block or remove ads** – instead, it overlays a black cover on ad containers and blurs video thumbnails to reduce exposure to inappropriate or provocative content.

---

## 📖 Why This Project?

Many YouTube ads and video thumbnails contain suggestive, misleading, or otherwise uncomfortable imagery. While we believe creators and platforms deserve fair ad revenue, we also believe users should have a **safer viewing experience**.

Instead of blocking ads (which violates YouTube’s Terms of Service), this app:
- Places a **black overlay** over ad elements so they are not visible.
- Applies a **blur effect** to video thumbnails to avoid unwanted visual distractions.

> ⚠️ **Important:** This app does **not** block, skip, or remove ads. Ads are still loaded and played (including audio), but their visual content is hidden. This approach respects YouTube’s policies while giving users more control over their visual experience.

---

## ✨ Features

- 🔒 **Privacy‑first** – no user data is collected or shared.
- 🎯 **Ad cover** – black overlay on all ad containers (including Shorts ads).
- 🌫️ **Thumbnail blur** – blurs video thumbnails (customizable level).
- ⚡ **Two‑Activity architecture** – main feed and video player are separated, so the feed never refreshes when returning from a video.
- 🚀 **Fast & lightweight** – optimized WebView with caching and hardware acceleration.
- 🛡️ **No root required** – works on any Android device (API 26+).

---

## 🔧 Installation

### Option 1: Download APK (from Releases)
1. Go to [Releases](https://github.com/AlphanixARB/AlphanixTube/releases)
2. Download `app-release.apk`
3. Install it on your Android device (enable "Install from unknown sources" if needed)

### Option 2: Build from Source
```bash
git clone https://github.com/YOUR_USERNAME/AlphanixTube.git
cd AlphanixTube
./gradlew assembleRelease
```

## 🤝 Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

**I would be more than happy to expand and improve this project with your help.**  
Whether you have ideas for new features, bug fixes, or UI/UX improvements – feel free to jump in!

Here's how you can contribute:

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

Before submitting a PR, please open an [Issue](https://github.com/YOUR_USERNAME/AlphanixTube/issues) to discuss what you'd like to change. This ensures we're aligned and avoids duplicate work.

---

**Your feedback and ideas matter – let's build something great together!** 🚀
