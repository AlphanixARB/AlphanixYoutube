(function() {
    var css = `
        /* ۱. تار کردن کاور ویدیوها */
        .ytThumbnailViewModelImage,
        ytm-thumbnail-cover img,
        .video-thumbnail-img,
        .ytCoreImageHost {
            filter: blur(25px) !important;
            transition: none !important;
        }

        /* ۲. سیاه کردن ویدیوهای تبلیغاتی */
        .html5-video-player.ad-showing .video-stream,
        .html5-video-player.ad-interrupting .video-stream,
        .ad-created.ad-showing video,
        .video-ads,
        .ytp-ad-player-overlay {
            filter: brightness(0) !important;
            pointer-events: none !important;
            background: black !important;
        }

        /* ۳. حذف بنرهای متنی */
        .ytp-ad-overlay-container, .ytd-ad-slot-renderer, ytm-ad-slot-renderer,
        .companion-ad-container, ytm-promoted-sparkles-web-renderer, ytm-single-ad-slot-renderer {
            display: none !important;
            opacity: 0 !important;
            visibility: hidden !important;
        }
    `;

    function applyUltimateStyle() {
        // تزریق به بالاترین سطح ممکن لایه رندر (حتی بالاتر از head)
        var target = document.documentElement || document.head;
        var style = document.getElementById('stylus-injected-asset');
        if (!style) {
            style = document.createElement('style');
            style.id = 'stylus-injected-asset';
            style.type = 'text/css';
            target.appendChild(style);
        }
        style.innerHTML = css;

        // نفوذ به لایه‌های پنهان Shadow DOM یوتیوب در صورت وجود
        var allElements = document.querySelectorAll('*');
        allElements.forEach(function(el) {
            if (el.shadowRoot) {
                var shadowStyle = el.shadowRoot.getElementById('stylus-shadow-asset');
                if (!shadowStyle) {
                    shadowStyle = document.createElement('style');
                    shadowStyle.id = 'stylus-shadow-asset';
                    shadowStyle.type = 'text/css';
                    el.shadowRoot.appendChild(shadowStyle);
                }
                shadowStyle.innerHTML = css;
            }
        });
    }

    // اجرای پرقدرت و مداوم برای پوشش اسکرول‌ها و ویدیوهای جدید
    applyUltimateStyle();
    setInterval(applyUltimateStyle, 300);
})();