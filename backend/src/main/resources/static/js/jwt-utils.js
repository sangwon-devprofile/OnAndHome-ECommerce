/**
 * ✅ JWT 공통 유틸리티 함수
 */

/**
 * JWT 토큰 가져오기
 */
function getAccessToken() {
    return localStorage.getItem('accessToken');
}

/**
 * JWT 헤더 생성
 */
function getAuthHeaders() {
    const accessToken = getAccessToken();
    const headers = { 'Content-Type': 'application/json' };
    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
        console.log('[getAuthHeaders] Authorization 헤더 추가 완료');
    } else {
        console.warn('[getAuthHeaders] accessToken이 없어 Authorization 헤더를 추가하지 않았습니다.');
    }
    return headers;
}

/**
 * 로그인 체크 (리다이렉트 포함)
 */
function checkLogin(redirectToLogin = true) {
    const accessToken = getAccessToken();
    console.log('[checkLogin] accessToken 확인:', accessToken ? '존재' : '없음');
    
    if (!accessToken) {
        console.warn('[checkLogin] 로그인 토큰이 없습니다.');
        if (redirectToLogin) {
            alert('로그인이 필요한 페이지입니다.');
            window.location.href = '/login';
        }
        return false;
    }
    
    console.log('[checkLogin] 로그인 상태 확인 완료');
    return true;
}

/**
 * 로그아웃 처리
 */
function handleLogout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userInfo');
        window.location.href = '/login?logout=true';
    }
}

/**
 * 헤더 네비게이션 로드 (JWT 기반)
 */
function loadHeaderNavigation() {
    const accessToken = getAccessToken();
    const headers = {};
    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`;
    }

    fetch('/api/user/session-info', { headers })
        .then(response => response.json())
        .then(data => {
            const headerNav = document.getElementById('headerNav');
            if (!headerNav) return;
            
            headerNav.innerHTML = '';

            if (data.loggedIn) {
                let navHTML = `
                    <li><a href="/user/my_page">마이페이지</a></li>
                    <li><a href="/user/board/notice/list">공지사항</a></li>
                `;
                if (data.isAdmin) {
                    navHTML += `<li><a href="/admin/dashboard"><b>관리자</b></a></li>`;
                }
                navHTML += `<li><a href="#" onclick="handleLogout(); return false;">로그아웃</a></li>`;
                headerNav.innerHTML = navHTML;
            } else {
                headerNav.innerHTML = `
                    <li><a href="/login">로그인</a></li>
                    <li><a href="/signup">회원가입</a></li>
                    <li><a href="/user/board/notice/list">공지사항</a></li>
                `;
            }
        })
        .catch(error => {
            console.error('세션 정보 조회 실패:', error);
            const headerNav = document.getElementById('headerNav');
            if (headerNav) {
                headerNav.innerHTML = `
                    <li><a href="/login">로그인</a></li>
                    <li><a href="/signup">회원가입</a></li>
                    <li><a href="/user/board/notice/list">공지사항</a></li>
                `;
            }
        });
}

/**
 * 카테고리 메뉴 로드
 */
function loadCategoryMenu() {
    const categoryData = [
        { parentName: 'TV/오디오', children: ['TV', '오디오'] },
        { parentName: '주방가전', children: ['냉장고', '전자렌지', '식기세척기'] },
        { parentName: '생활가전', children: ['세탁기', '청소기'] },
        { parentName: '에어컨/공기청정기', children: ['에어컨', '공기청정기'] },
        { parentName: '기타', children: ['정수기', '안마의자', 'PC'] }
    ];

    const categoryMenu = document.getElementById('categoryMenu');
    if (!categoryMenu) return;
    
    categoryMenu.innerHTML = '';

    categoryData.forEach(category => {
        const li = document.createElement('li');
        const parentLink = document.createElement('a');
        parentLink.href = '#';
        parentLink.textContent = category.parentName;
        parentLink.onclick = (e) => { e.preventDefault(); return false; };
        li.appendChild(parentLink);

        if (category.children && category.children.length > 0) {
            const depth2 = document.createElement('ul');
            depth2.className = 'depth2';

            category.children.forEach(child => {
                const childLi = document.createElement('li');
                const childLink = document.createElement('a');
                childLink.href = `/user/product/category?category=${encodeURIComponent(child)}`;
                childLink.textContent = child;
                childLi.appendChild(childLink);
                depth2.appendChild(childLi);
            });

            li.appendChild(depth2);
        }

        categoryMenu.appendChild(li);
    });
}

/**
 * 가격 포맷팅
 */
function formatPrice(price) {
    if (!price) return '0';
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

/**
 * HTML 이스케이프
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
