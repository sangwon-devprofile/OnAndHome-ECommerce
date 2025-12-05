
document.addEventListener('DOMContentLoaded', () => {
  const root = document.querySelector('.on-side-menu-nav .side-menu-list');
  if (!root) return;

  // 유틸: 부드럽게 열기/닫기
  const openSub = (li, sub) => {
    li.classList.add('is-open');
    // 먼저 auto 높이를 얻기 위해 일시적으로 펼쳐 측정
    sub.style.display = 'block';
    const target = sub.scrollHeight + 'px';
    sub.style.maxHeight = target;
    // 전환 끝나면 max-height 초기화(콘텐츠 높이 변화 대응)
    sub.addEventListener('transitionend', function tidy(e){
      if (e.propertyName === 'max-height') sub.style.maxHeight = sub.scrollHeight + 'px';
      sub.removeEventListener('transitionend', tidy);
    });
  };

  const closeSub = (li, sub) => {
    // 현재 높이를 기준으로 0으로 전환
    sub.style.maxHeight = sub.scrollHeight + 'px';
    requestAnimationFrame(() => {
      li.classList.remove('is-open');
      sub.style.maxHeight = '0px';
      sub.addEventListener('transitionend', function tidy(e){
        if (e.propertyName === 'max-height') sub.style.display = 'none';
        sub.removeEventListener('transitionend', tidy);
      });
    });
  };

  // 모든 1뎁스 li 설정
  root.querySelectorAll(':scope > li').forEach((li) => {
    const a = li.querySelector(':scope > a');
    const sub = li.querySelector(':scope > ul');
    if (!a || !sub) return; // 2뎁스 없는 항목은 건너뛰기

    // 초기 숨김
    sub.style.display = 'none';
    sub.style.maxHeight = '0px';

    a.addEventListener('click', (e) => {
      // 토글만 하고 싶으면 기본 이동 막기
      e.preventDefault();

      const isOpen = li.classList.contains('is-open');

      // 하나만 열리게 하려면: 다른 것들 닫기
      root.querySelectorAll(':scope > li.is-open').forEach((opened) => {
        if (opened === li) return;
        const openedSub = opened.querySelector(':scope > ul');
        if (openedSub) closeSub(opened, openedSub);
      });

      // 클릭한 것 토글
      isOpen ? closeSub(li, sub) : openSub(li, sub);
    });

    // 옵션: 열려있는 상태에서 Alt+클릭 시 실제 링크로 이동하는 동작을 원하면 아래 사용
    // a.addEventListener('click', (e) => {
    //   if (li.classList.contains('is-open') && e.altKey) {
    //     window.location.href = a.getAttribute('href') || '#';
    //   }
    // });
  });
});


document.addEventListener('DOMContentLoaded', function () {
  const tops = document.querySelectorAll('.menu-wrapper > ul.gnb > li');

  tops.forEach(li => {
    // 마우스
    li.addEventListener('mouseenter', () => li.classList.add('is-open'));
    li.addEventListener('mouseleave', () => li.classList.remove('is-open'));

    // 터치/클릭: 2뎁스 있을 때 첫 클릭은 펼치기만
    const topLink = li.querySelector(':scope > a');
    const depth2  = li.querySelector(':scope > .depth2');
    if (topLink && depth2) {
      topLink.addEventListener('click', (e) => {
        const opened = li.classList.contains('is-open');
        // 이미 열려있으면 통과(실제 링크 이동), 아니면 펼치고 이동 막기
        if (!opened) {
          e.preventDefault();
          // 다른 열림 닫기
          tops.forEach(other => { if (other !== li) other.classList.remove('is-open'); });
          li.classList.add('is-open');
        }
      });
    }

    // Esc로 닫기
    li.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        li.classList.remove('is-open');
        topLink && topLink.focus();
      }
    });
  });

  // 바깥 클릭 시 전체 닫기
  document.addEventListener('click', (e) => {
    const wrap = document.querySelector('.menu-wrapper');
    if (!wrap.contains(e.target)) {
      document.querySelectorAll('.menu-wrapper .is-open').forEach(el => el.classList.remove('is-open'));
    }
  });
});


document.addEventListener('DOMContentLoaded', function () {
  const wrapper = document.querySelector('.main-slide-wrapper');
  if (!wrapper) return;

  // 기존 .main-slide를 트랙으로 감싸기
  const originalSlides = Array.from(wrapper.querySelectorAll('.main-slide'));
  if (originalSlides.length === 0) return;

  const track = document.createElement('div');
  track.className = 'main-slide-track';
  
  // 원본 슬라이드를 트랙에 추가
  originalSlides.forEach(slide => track.appendChild(slide));
  
  // 첫 번째 슬라이드를 클론하여 마지막에 추가 (무한 루프용)
  const firstClone = originalSlides[0].cloneNode(true);
  firstClone.classList.add('clone');
  track.appendChild(firstClone);
  
  wrapper.appendChild(track);

  // 왼쪽 하단 점 만들기
  const dotsWrap = document.createElement('div');
  dotsWrap.className = 'slide-dots';
  wrapper.appendChild(dotsWrap);

  let current = 0;
  const total = originalSlides.length; // 클론 제외한 원본 개수
  let isTransitioning = false;

  // 점 버튼 생성 (클론은 제외)
  const dots = originalSlides.map((_, idx) => {
    const dot = document.createElement('button');
    dot.type = 'button';
    dot.className = 'slide-dot';
    dot.setAttribute('aria-label', `${idx + 1}번 슬라이드로 이동`);
    dot.addEventListener('click', () => {
      if (!isTransitioning) {
        goTo(idx);
        resetTimer();
      }
    });
    dotsWrap.appendChild(dot);
    return dot;
  });

  function updateDots() {
    // current가 total이면 실제로는 0번 클론이므로 0번 점 활성화
    const activeIndex = current === total ? 0 : current;
    dots.forEach((d, i) => d.classList.toggle('is-active', i === activeIndex));
  }

  function goTo(index, smooth = true) {
    if (isTransitioning) return;
    
    current = index;
    const x = -current * 100;
    
    if (smooth) {
      track.style.transition = 'transform 0.5s ease-in-out';
    } else {
      track.style.transition = 'none';
    }
    
    track.style.transform = `translateX(${x}%)`;
    updateDots();
    
    // 클론 위치에서 실제 위치로 즉시 이동
    if (current === total) {
      isTransitioning = true;
      setTimeout(() => {
        track.style.transition = 'none';
        current = 0;
        track.style.transform = `translateX(0%)`;
        updateDots();
        isTransitioning = false;
      }, 500); // transition 시간과 동일
    }
  }

  // 초기 세팅
  goTo(0, false);

  // 자동재생: 2초 간격
  let timer = setInterval(() => {
    if (!isTransitioning) {
      goTo(current + 1);
    }
  }, 2000);

  // 호버 시 일시정지 / 해제 시 재시작
  wrapper.addEventListener('mouseenter', () => clearInterval(timer));
  wrapper.addEventListener('mouseleave', () => resetTimer());

  // 포커스 접근성: 점에 키보드 포커스 들어오면 일시정지, 떠나면 재시작
  dotsWrap.addEventListener('focusin', () => clearInterval(timer));
  dotsWrap.addEventListener('focusout', () => resetTimer());

  // 모션 최소화 환경이면 자동재생 비활성(선택)
  if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    clearInterval(timer);
  }

  function resetTimer() {
    clearInterval(timer);
    timer = setInterval(() => {
      if (!isTransitioning) {
        goTo(current + 1);
      }
    }, 2000);
  }
});



document.addEventListener('DOMContentLoaded', () => {
  const payEl = document.querySelector('#btnPay');
  if (!payEl) {
    console.warn('[pay] #btnPay 요소를 찾지 못했습니다.');
    return;
  }

  payEl.addEventListener('click', (e) => {
    // a, button 둘 다 안전하게 기본동작 방지
    e.preventDefault();

    const ok = confirm('결제하시겠습니까?');
    if (ok) {
      window.location.href = '/user/order.html';
    }
    // 취소 시: 아무 것도 하지 않음 (현재 페이지 유지)
  });
});


document.addEventListener('DOMContentLoaded', function () {
  const withdrawBtn = document.getElementById('btnWithdraw');
  if (!withdrawBtn) return;

  withdrawBtn.addEventListener('click', function (e) {
    e.preventDefault(); // a태그 기본 이동 막기

    const confirmed = confirm('회원탈퇴하시겠습니까?');
    if (confirmed) {
      alert('회원 탈퇴가 완료되었습니다.');
      window.location.href = '/user/index.html'; // 메인페이지로 이동
    } else {
      // 취소 시 아무 동작 안 함 (현재 페이지 유지)
      return false;
    }
  });
});


/**
 * 헤더 네비게이션 로드 (JWT 기반)
 * 모든 페이지에서 사용 가능한 공통 함수
 */
function loadHeaderNavigation() {
  const accessToken = localStorage.getItem('accessToken');
  const headers = {};
  
  if (accessToken) {
    headers['Authorization'] = `Bearer ${accessToken}`;
  }

  fetch('/api/user/session-info', { headers: headers })
    .then(response => response.json())
    .then(data => {
      const headerNav = document.getElementById('headerNav');
      if (!headerNav) {
        console.warn('headerNav 요소를 찾을 수 없습니다.');
        return;
      }
      
      headerNav.innerHTML = '';

      if (data.loggedIn) {
        // 로그인 상태
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
        // 비로그인 상태
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
 * 로그아웃 처리 (JWT 토큰 삭제)
 */
function handleLogout() {
  if (confirm('로그아웃 하시겟습니까?')) {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userInfo');
    window.location.href = '/login?logout=true';
  }
}


document.addEventListener("DOMContentLoaded", function () {
  const categoryData = {
    tv_audio: ["TV", "오디오"],
    kitchen: ["냉장고", "전자렌지", "식기세척기"],
    living: ["세탁기", "청소기"],
    air: ["에어컨", "공기청정기"],
    etc: ["정수기", "안마의자", "PC"]
  };

  const category1 = document.getElementById("category1");
  const category2 = document.getElementById("category2");

  category1.addEventListener("change", function () {
    const selected = this.value;
    const subOptions = categoryData[selected] || [];

    // 2뎁스 초기화
    category2.innerHTML = '<option value="">2뎁스 선택</option>';

    // 선택한 1뎁스의 하위 옵션 추가
    subOptions.forEach(item => {
      const option = document.createElement("option");
      option.value = item;
      option.textContent = item;
      category2.appendChild(option);
    });
  });
});


document.addEventListener("DOMContentLoaded", function () {
  const fileBoxes = document.querySelectorAll(".file-box");

  fileBoxes.forEach((box) => {
    const fileBtn = box.querySelector(".file-btn");
    const fileInput = box.querySelector(".file-input");
    const fileName = box.querySelector(".file-name");
    const fileClear = box.querySelector(".file-clear");

    // 파일첨부 버튼 클릭 → 파일선택창 열기
    fileBtn.addEventListener("click", () => fileInput.click());

    // 파일 선택 시 파일명 표시
    fileInput.addEventListener("change", () => {
      if (fileInput.files.length > 0) {
        fileName.value = fileInput.files[0].name;
      } else {
        fileName.value = "";
      }
    });

    // X 버튼 클릭 시 파일 해제
    fileClear.addEventListener("click", () => {
      fileInput.value = ""; // 파일 선택 해제
      fileName.value = "";  // 표시된 이름 지우기
    });
  });
});


