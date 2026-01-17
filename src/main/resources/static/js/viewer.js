class ComicViewer {
    constructor(comicId, comicFolder, totalPages) {
        this.comicId = comicId;
        this.comicFolder = comicFolder;
        this.totalPages = totalPages;
        this.currentPage = 1;
        this.imagesLoaded = new Set();
        this.readerElement = document.getElementById('comicReader');
        this.currentPageElement = document.getElementById('currentPage');
        this.totalPagesElement = document.getElementById('totalPages');
        this.prevBtn = document.getElementById('prevBtn');
        this.nextBtn = document.getElementById('nextBtn');

        this.init();
    }

    init() {
        this.totalPagesElement.textContent = this.totalPages;
        this.setupControls();
        this.loadPage(this.currentPage);
        this.preloadAdjacentPages();
        this.setupKeyboardControls();
    }

    setupControls() {
        this.prevBtn.addEventListener('click', () => this.prevPage());
        this.nextBtn.addEventListener('click', () => this.nextPage());
    }

    setupKeyboardControls() {
        document.addEventListener('keydown', (e) => {
            switch(e.key) {
                case 'ArrowUp':
                case 'PageUp':
                    e.preventDefault();
                    this.prevPage();
                    break;
                case 'ArrowDown':
                case 'PageDown':
                case ' ':
                    e.preventDefault();
                    this.nextPage();
                    break;
            }
        });
    }

    prevPage() {
        if (this.currentPage > 1) {
            this.currentPage--;
            this.updateViewer();
        }
    }

    nextPage() {
        if (this.currentPage < this.totalPages) {
            this.currentPage++;
            this.updateViewer();
        }
    }

    updateViewer() {
        this.loadPage(this.currentPage);
        this.updateControls();
        this.preloadAdjacentPages();
        this.updateURL();
        this.scrollToTop();
    }

    loadPage(pageNumber) {
        this.currentPageElement.textContent = pageNumber;

        // 如果图片已经加载过，直接显示
        if (this.imagesLoaded.has(pageNumber)) {
            this.showPage(pageNumber);
        } else {
            this.loadImage(pageNumber);
        }
    }

    loadImage(pageNumber) {
        const readerElement = this.readerElement;
        readerElement.innerHTML = `
            <div class="loading">
                <i class="fas fa-spinner fa-spin fa-2x"></i>
                <p>正在加载第 ${pageNumber} 页...</p>
            </div>
        `;

        const img = new Image();
        img.src = `/uploads/comics/${this.comicFolder}/${pageNumber}.jpg`;
        img.alt = `第 ${pageNumber} 页`;
        img.className = 'comic-page';

        img.onload = () => {
            readerElement.innerHTML = '';
            readerElement.appendChild(img);
            setTimeout(() => img.classList.add('loaded'), 100);
            this.imagesLoaded.add(pageNumber);
        };

        img.onerror = () => {
            readerElement.innerHTML = `
                <div class="loading">
                    <i class="fas fa-exclamation-circle fa-2x"></i>
                    <p>无法加载第 ${pageNumber} 页</p>
                    <p>请检查网络连接或刷新页面</p>
                </div>
            `;
        };
    }

    showPage(pageNumber) {
        const readerElement = this.readerElement;
        const existingImg = readerElement.querySelector('.comic-page');

        if (existingImg) {
            existingImg.classList.remove('loaded');
            setTimeout(() => {
                existingImg.src = `/uploads/comics/${this.comicFolder}/${pageNumber}.jpg`;
                existingImg.alt = `第 ${pageNumber} 页`;
                setTimeout(() => existingImg.classList.add('loaded'), 100);
            }, 300);
        }
    }

    preloadAdjacentPages() {
        const pagesToPreload = [];

        for (let i = 1; i <= 3; i++) {
            const nextPage = this.currentPage + i;
            if (nextPage <= this.totalPages && !this.imagesLoaded.has(nextPage)) {
                pagesToPreload.push(nextPage);
            }

            const prevPage = this.currentPage - i;
            if (prevPage >= 1 && !this.imagesLoaded.has(prevPage)) {
                pagesToPreload.push(prevPage);
            }
        }

        pagesToPreload.forEach(page => {
            const img = new Image();
            img.src = `/uploads/comics/${this.comicFolder}/${page}.jpg`;
            img.onload = () => this.imagesLoaded.add(page);
        });
    }

    updateControls() {
        this.prevBtn.disabled = this.currentPage === 1;
        this.nextBtn.disabled = this.currentPage === this.totalPages;

        if (this.currentPage === 1) {
            this.prevBtn.classList.add('disabled');
        } else {
            this.prevBtn.classList.remove('disabled');
        }

        if (this.currentPage === this.totalPages) {
            this.nextBtn.classList.add('disabled');
        } else {
            this.nextBtn.classList.remove('disabled');
        }
    }

    updateURL() {
        const url = new URL(window.location);
        url.searchParams.set('page', this.currentPage);
        window.history.replaceState({}, '', url);
    }

    scrollToTop() {
        const readerTop = this.readerElement.getBoundingClientRect().top;
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const targetTop = scrollTop + readerTop - 100;

        window.scrollTo({
            top: targetTop,
            behavior: 'smooth'
        });
    }
}

// 全局初始化函数
function initViewer(comicId, comicFolder, totalPages) {
    window.comicViewer = new ComicViewer(comicId, comicFolder, totalPages);
}