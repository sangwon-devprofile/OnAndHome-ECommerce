const ui = {
  init: function () {
    if (document.querySelector(".select-ux")) {
      this.select.init();
    }
    if (document.querySelector(".tab-ux")) {
      this.tab.init();
    }
    if (document.querySelector(".sidebar")) {
      this.gnb.init();
    }
  },

  //Select
  select: {
    elems: document.querySelectorAll(".select-ux"),
    init: function () {
      const _this = this;
      _this.event();
    },
    event: function () {
      const _this = this;
      _this.elems.forEach((elem) => {
        const btn = elem.querySelector("[data-toggle]");
        const list = elem.querySelector("[data-options]");
        const items = list?.querySelectorAll("li") || [];
        btn.addEventListener("click", () => {
          list.style.display =
            list.style.display == "block" ? "none" : "block";
        });
        _this.option(btn, list, items);
        _this.close(elem, list);
      });
    },
    option: function (b, l, i) {
      i.forEach((s) => {
        s.addEventListener("click", () => {
          i.forEach((t) => t.classList.remove("selected"));
          s.classList.add("selected");
          b.textContent = s.textContent;
          b.dataset.value = s.dataset.value;
          b.focus();
          l.style.display = "none";
        });
        s.addEventListener("keydown", () => {
          i.forEach((t) => t.classList.remove("selected"));
          s.classList.add("selected");
          b.textContent = s.textContent;
          b.dataset.value = s.dataset.value;
          b.focus();
          l.style.display = "none";
        });
      });
    },
    close: function (el, l) {
      document.addEventListener("click", (e) => {
        if (!el.contains(e.target)) {
          l.style.display = "none";
        }
      });
    },
  },

  //Tab
  tab: {
    tabs: document.querySelectorAll(".tab-ux"),
    init: function () {
      const _this = this;
      _this.event();
    },
    event: function () {
      const _this = this;
      _this.tabs.forEach((tab) => {
        const btns = tab.querySelectorAll(".tab-ux__btn");
        const contents = tab.querySelectorAll(".tab-ux__content");
        btns.forEach(b => {
          b.addEventListener("click", () => {
            btns.forEach(b => b.classList.remove('active'));
            contents.forEach(c => c.classList.remove('active'));
            b.classList.add('active');
            const targetContent = document.getElementById(b.getAttribute('data-tab'));
            targetContent.classList.add("active");
          });
        });
        const firstTab = btns[0];
        const firstContent = document.getElementById(firstTab.getAttribute('data-tab'));
        firstTab.classList.add('active');
        firstContent.classList.add('active');
      });
    }
  },

  tab: {
    tabs: document.querySelectorAll(".tab-menu-ux"),
    init: function () {
      const _this = this;
      _this.event();
    },
    event: function () {
      const _this = this;
      _this.tabs.forEach((tab) => {
        const btns = tab.querySelectorAll(".tab-menu__btn");
        const contents = tab.querySelectorAll(".tab-menu__content");
        btns.forEach(b => {
          b.addEventListener("click", () => {
            btns.forEach(b => b.classList.remove('active'));
            contents.forEach(c => c.classList.remove('active'));
            b.classList.add('active');
            const targetContent = document.getElementById(b.getAttribute('data-tab'));
            targetContent.classList.add("active");
          });
        });
        const firstTab = btns[0];
        const firstContent = document.getElementById(firstTab.getAttribute('data-tab'));
        firstTab.classList.add('active');
        firstContent.classList.add('active');
      });
    }
  },

  //Gnb
  gnb: {
    sidebar: document.getElementById('sidebar'),
    subPanel: document.getElementById('subPanel'),
    init: function () {
      const _this = this;
      _this.depth1();
      // _this.close();
    },
    depth1: function(){
      const _this = this;
      const ul= document.createElement('ul');
      ul.classList.add('sidebar__nav');
      ul.dataset.depth = "depth-1";
      menuData.forEach(item => {
        const li = document.createElement('li');
        const a = document.createElement('a');
        a.href = item.link || '#';
        const icon = document.createElement('i');
        icon.className = item.icon || '';
        const text = document.createElement('span');
        text.textContent = item.title;
        a.appendChild(icon);
        a.appendChild(text);
        li.appendChild(a);
        li.addEventListener('mouseenter', (e) => {
          subPanel.innerHTML = '';
          _this.subPanel.style.display = 'none';
          // 2뎁스가 있는 경우
          if(item.children){
            _this.depth2(item.children); //depth2 호출
          }
        });
      ul.appendChild(li);
      _this.sidebar.prepend(ul);
    });
    },
    depth2: function(s){
      const _this = this;
      const ul = document.createElement('ul');
      ul.dataset.depth = 'depth-2';
      s.forEach(item => {
        const li = document.createElement('li');
        const a = document.createElement('a');
        a.href = item.link || '#';
        const text = document.createElement('span');
        text.textContent = item.title;
        a.appendChild(text);
        li.appendChild(a);
        // 3뎁스가 있는 경우
        if(item.children){
          li.addEventListener('click', e =>{
            const open = ul.querySelector('[data-depth = "depth-3"]');
            if (open){
              open.remove();
              return;
            }
            const dep3 = _this.depth3(item.children); //depth3 호출
            li.appendChild(dep3);
          });
        }
        ul.appendChild(li);
      });
      _this.subPanel.appendChild(ul);
      _this.subPanel.style.display = 'block';
    },
    depth3: function(n){
      const ul = document.createElement('ul');
      ul.dataset.depth = 'depth-3';
      n.forEach(item => {
        const li = document.createElement('li');
        const a = document.createElement('a');
        a.href = item.link || '#';
        a.textContent = item.title;
        a.addEventListener('click', function(e){
          e.stopPropagation();
        });
        li.appendChild(a);
        ul.appendChild(li);
      });
      return ul;
    },
    close: function(){
      const _this = this;
      let isInsideBar = false;
      let isInsidePanel = false;
      let hideTimer;
      function checkMouseLeave() {
        clearTimeout(hideTimer);
        hideTimer = setTimeout(() => {
          if (!isInsideBar && !isInsidePanel) {
            _this.subPanel.style.display = 'none';
          }
        }, 100); //flickering 방지
      }
      _this.sidebar.addEventListener('mouseenter', () => {
        isInsideBar = true;
      });
      _this.sidebar.addEventListener('mouseleave', () => {
        isInsideBar = false;
        checkMouseLeave();
      });
      _this.subPanel.addEventListener('mouseenter', () => {
        isInsidePanel = true;
      });
      _this.subPanel.addEventListener('mouseleave', () => {
        isInsidePanel = false;
        checkMouseLeave();
      });
    }
  }
};

window.onload = function () {
  ui.init();
};



// uploader

const dropzone = document.getElementById("dropzone");
const fileList = document.getElementById("fileList");
const addBtn = document.getElementById("addBtn");
const fileInput = document.getElementById("fileInput");

addBtn.addEventListener("click", () => {
  fileInput.click();
});

dropzone.addEventListener("dragover", (e) => {
  e.preventDefault();
  dropzone.classList.add("dragging");
});

dropzone.addEventListener("dragleave", () => {
  dropzone.classList.remove("dragging");
});

dropzone.addEventListener("drop", (e) => {
  e.preventDefault();
  dropzone.classList.remove("dragging");
  handleFileSelect(e);
});

fileInput.addEventListener("change", handleFileSelect);

function handleFileSelect(event) {
  const files = event.dataTransfer
    ? event.dataTransfer.files
    : event.target.files;

  if (files.length > 0) {
    dropzone.classList.add("has-files");
  }

  Array.from(files).forEach((file) => {
    const fileItem = document.createElement("div");
    fileItem.className = "file-item";
    const fileSize = (file.size / 1024).toFixed(2) + " KB";

    const url = URL.createObjectURL(file);

    fileItem.innerHTML = `
      <div class="file-info">
        <p>${file.name}</p>
        <span class="file-size">(${fileSize})</span>
      </div>
      <div class="file-actions">
        <span class="icon-preview" title="미리보기"></span>
        <a href="${url}" download="${file.name}">
          <span class="icon-download" title="다운로드"></span>
        </a>
        <span class="icon-delete" title="삭제" onclick="removeFile(this)"></span>
      </div>
    `;
    fileList.appendChild(fileItem);
  });
}

function removeFile(el) {
  el.closest(".file-item").remove();
  if (fileList.children.length === 0) {
    dropzone.classList.remove("has-files");
  }
}


// ---------- 모달 1 ----------
let isExpanded1 = false;

function openModal1() {
  const modal1 = document.getElementById('modal1');
  if (!modal1) return;
  const popup = modal1.querySelector('.modalbox');
  const expandBtn = modal1.querySelector('.modalbox-expand');
  modal1.classList.add('show');
  if (popup) popup.style.width = '500px';
  if (expandBtn) expandBtn.classList.remove('modalbox-fold');
  isExpanded1 = false;
}

function closeModal1() {
  const modal1 = document.getElementById('modal1');
  if (modal1) modal1.classList.remove('show');
}

function expandModal1() {
  const modal1 = document.getElementById('modal1');
  if (!modal1) return;
  const popup = modal1.querySelector('.modalbox');
  const expandBtn = modal1.querySelector('.modalbox-expand');

  if (!popup || !expandBtn) return;

  if (isExpanded1) {
    popup.style.width = '500px';
    expandBtn.classList.remove('modalbox-fold');
    isExpanded1 = false;
  } else {
    popup.style.width = '90%';
    expandBtn.classList.add('modalbox-fold');
    isExpanded1 = true;
  }
}

// ---------- 모달 2 ----------
let isExpanded2 = false;

function openModal2() {
  const modal2 = document.getElementById('modal2');
  if (!modal2) return;
  const popup = modal2.querySelector('.modalbox');
  const expandBtn = modal2.querySelector('.modalbox-expand');
  modal2.classList.add('show');
  if (popup) popup.style.width = '1000px';
  if (expandBtn) expandBtn.classList.remove('modalbox-fold');
  isExpanded2 = false;
}

function closeModal2() {
  const modal2 = document.getElementById('modal2');
  if (modal2) modal2.classList.remove('show');
}

function expandModal2() {
  const modal2 = document.getElementById('modal2');
  const popup = modal2.querySelector('.modalbox');
  const expandBtn = modal2.querySelector('.modalbox-expand');

  if (isExpanded2) {
    popup.style.width = '1000px'; // 기존대로
    popup.classList.remove('modalbox-expanded'); // 펼침 해제 시 클래스 제거
    expandBtn.classList.remove('modalbox-fold');
    isExpanded2 = false;
  } else {
    popup.style.width = (window.innerWidth - 36) + 'px';
    popup.classList.add('modalbox-expanded'); // ✅ 펼침 시 높이 자동 설정
    expandBtn.classList.add('modalbox-fold');
    isExpanded2 = true;
  }
}

// ---------- 공통 바깥 클릭 닫기 + 업로더 안전 실행 ----------
document.addEventListener('DOMContentLoaded', function () {
  const modal1 = document.getElementById('modal1');
  if (modal1) {
    modal1.addEventListener('click', function (e) {
      if (e.target === this) {
        closeModal1();
      }
    });
  }

  const modal2 = document.getElementById('modal2');
  if (modal2) {
    modal2.addEventListener('click', function (e) {
      if (e.target === this) {
        closeModal2();
      }
    });
  }

  // ✅ uploader 코드도 안전하게 실행
  const dropzone = document.getElementById("dropzone");
  const fileList = document.getElementById("fileList");
  const addBtn = document.getElementById("addBtn");
  const fileInput = document.getElementById("fileInput");

  if (dropzone && fileList && addBtn && fileInput) {
    addBtn.addEventListener("click", () => {
      fileInput.click();
    });

    dropzone.addEventListener("dragover", (e) => {
      e.preventDefault();
      dropzone.classList.add("dragging");
    });

    dropzone.addEventListener("dragleave", () => {
      dropzone.classList.remove("dragging");
    });

    dropzone.addEventListener("drop", (e) => {
      e.preventDefault();
      dropzone.classList.remove("dragging");
      handleFileSelect(e);
    });

    fileInput.addEventListener("change", handleFileSelect);
  }

  function handleFileSelect(event) {
    const files = event.dataTransfer
      ? event.dataTransfer.files
      : event.target.files;

    if (files.length > 0) {
      dropzone.classList.add("has-files");
    }

    Array.from(files).forEach((file) => {
      const fileItem = document.createElement("div");
      fileItem.className = "file-item";
      const fileSize = (file.size / 1024).toFixed(2) + " KB";
      const url = URL.createObjectURL(file);

      fileItem.innerHTML = `
        <div class="file-info">
          <p>${file.name}</p>
          <span class="file-size">(${fileSize})</span>
        </div>
        <div class="file-actions">
          <span class="icon-preview" title="미리보기"></span>
          <a href="${url}" download="${file.name}">
            <span class="icon-download" title="다운로드"></span>
          </a>
          <span class="icon-delete" title="삭제" onclick="removeFile(this)"></span>
        </div>
      `;
      fileList.appendChild(fileItem);
    });
  }

  function removeFile(el) {
    el.closest(".file-item").remove();
    if (fileList.children.length === 0) {
      dropzone.classList.remove("has-files");
    }
  }
});





  

// select

document.querySelectorAll('.select-wrap').forEach(wrapper => {
  const input = wrapper.querySelector('.select-input');
  const isMulti = wrapper.dataset.multiselect === "true";
  const options = wrapper.querySelectorAll('.select-option');
  const cancelBtn = wrapper.querySelector('.btn--quarternary-outline');
  const confirmBtn = wrapper.querySelector('.btn--primary');
  const selectAllCheckbox = wrapper.querySelector('.select-all');
  const allCheckboxes = wrapper.querySelectorAll('.select-option input[type="checkbox"]');

  let tempSelection = [];

  // 다중 선택이 아니면 체크박스와 액션 숨김
  if (!isMulti) {
    const checkboxes = wrapper.querySelectorAll('input[type="checkbox"]');
    checkboxes.forEach(chk => chk.parentElement.style.display = 'none');

    const selectActions = wrapper.querySelector('.select-actions');
    if (selectActions) selectActions.style.display = 'none';
  }

  // 열기/닫기 토글 + 너비 조정
  input.addEventListener('click', () => {
    wrapper.classList.toggle('open');
    if (isMulti) saveTempSelection();
    adjustSelectWidth(wrapper); // 너비 조정 호출
  });

  options.forEach(option => {
    const checkbox = option.querySelector('input[type="checkbox"]');
    if (isMulti && checkbox) {
      checkbox.addEventListener('change', () => {
        updateMultiSelectInput();
        syncSelectAllCheckbox();
      });
    } else {
      option.addEventListener('click', () => {
        input.value = option.textContent.trim();
        wrapper.classList.remove('open');
      });
    }
  });

  // 전체 선택/해제 동작
  if (isMulti && selectAllCheckbox) {
    selectAllCheckbox.addEventListener('change', () => {
      const checked = selectAllCheckbox.checked;
      allCheckboxes.forEach(chk => chk.checked = checked);
      updateMultiSelectInput();
    });
  }

  // 취소 버튼 동작
  cancelBtn?.addEventListener('click', () => {
    restoreTempSelection();
    wrapper.classList.remove('open');
  });

  // 확인 버튼 동작
  confirmBtn?.addEventListener('click', () => {
    wrapper.classList.remove('open');
  });

  // 체크된 항목 입력 업데이트
  function updateMultiSelectInput() {
    const checkedBoxes = wrapper.querySelectorAll('.select-option input[type="checkbox"]:checked');
    const checkedCount = checkedBoxes.length;

    if (checkedCount === 0) {
      input.value = '선택하세요';
    } else {
      const selectedTexts = Array.from(checkedBoxes).map(chk => chk.parentElement.textContent.trim());
      input.value = selectedTexts.join(', ');
    }
  }

  // 현재 상태 임시 저장
  function saveTempSelection() {
    tempSelection = Array.from(allCheckboxes).map(chk => chk.checked);
  }

  // 임시 저장 상태 복원
  function restoreTempSelection() {
    allCheckboxes.forEach((chk, i) => {
      chk.checked = tempSelection[i];
    });
    updateMultiSelectInput();
    syncSelectAllCheckbox();
  }

  // 개별 체크박스 변경 시 전체선택 상태 동기화
  function syncSelectAllCheckbox() {
    const total = allCheckboxes.length;
    const checkedCount = wrapper.querySelectorAll('.select-option input[type="checkbox"]:checked').length;
    if (selectAllCheckbox) selectAllCheckbox.checked = total > 0 && total === checkedCount;
  }

  // 외부 클릭 시 닫기
  document.addEventListener('click', e => {
    if (!wrapper.contains(e.target)) {
      wrapper.classList.remove('open');
    }
  });

  // 옵션 길이에 맞춰 래퍼 너비 조정
  function adjustSelectWidth(wrapper) {
    const options = wrapper.querySelectorAll('.select-option');
    const tempSpan = document.createElement('span');
    tempSpan.style.visibility = 'hidden';
    tempSpan.style.position = 'absolute';
    tempSpan.style.whiteSpace = 'nowrap';
    tempSpan.style.fontSize = window.getComputedStyle(input).fontSize;
    document.body.appendChild(tempSpan);

    let maxWidth = wrapper.offsetWidth;

    options.forEach(option => {
      tempSpan.textContent = option.textContent.trim();
      const width = tempSpan.offsetWidth + 40; // 패딩 보정
      if (width > maxWidth) {
        maxWidth = width;
      }
    });

    document.body.removeChild(tempSpan);

    wrapper.style.width = maxWidth + 'px';
  }
});






// 자동완성폼

const suggestions = ['자동완성 항목 1', '자동완성 항목 2', '자동완성 항목 3'];

function showAutoComplete(wrapperId, value) {
  const wrapper = document.getElementById(wrapperId);
  const box = wrapper.querySelector('.auto-srch-box');
  const input = wrapper.querySelector('.input');

  if (value.trim() === '') {
    box.style.display = 'none';
    box.innerHTML = '';
    return;
  }

  box.innerHTML = suggestions.map(item => `<li onclick="selectAutoComplete('${wrapperId}', '${item}')">${item}</li>`).join('');
  box.style.display = 'block';

  bindEscapeToInput(wrapperId); // ESC 바인딩
}

function selectAutoComplete(wrapperId, value) {
  const wrapper = document.getElementById(wrapperId);
  const input = wrapper.querySelector('.input');
  const box = wrapper.querySelector('.auto-srch-box');
  input.value = value;
  box.style.display = 'none';
  box.innerHTML = '';
}

// ESC 이벤트 바인딩 (한 번만 바인딩 되도록)
function bindEscapeToInput(wrapperId) {
  const wrapper = document.getElementById(wrapperId);
  const input = wrapper.querySelector('.input');
  const box = wrapper.querySelector('.auto-srch-box');

  function escHandler(e) {
    if (e.key === 'Escape') {
      input.value = '';
      box.style.display = 'none';
      box.innerHTML = '';
      input.removeEventListener('keydown', escHandler); // 중복 바인딩 방지
    }
  }

  input.removeEventListener('keydown', escHandler); // 기존 바인딩 제거
  input.addEventListener('keydown', escHandler);    // 새로 바인딩
}




// pagination

  document.addEventListener("DOMContentLoaded", function() {
    const pagination = document.querySelector(".pagination");
    const pageButtons = pagination.querySelectorAll("button[data-page]");
    let currentPage = 3;

    function updateActive(page) {
      pageButtons.forEach(btn => {
        btn.classList.toggle("active", Number(btn.dataset.page) === page);
      });
      currentPage = page;
    }

    pagination.addEventListener("click", function(e) {
      const btn = e.target.closest("button");
      if (!btn) return;

      const page = btn.dataset.page;
      const action = btn.dataset.action;

      if (page) {
        updateActive(Number(page));
      } else if (action) {
        if (action === "first") {
          updateActive(1);
        } else if (action === "prev" && currentPage > 1) {
          updateActive(currentPage - 1);
        } else if (action === "next" && currentPage < 5) {
          updateActive(currentPage + 1);
        } else if (action === "last") {
          updateActive(5);
        }
      }
    });
  });

  	// Datepicker
	class GridDatepicker {
		constructor(props) {
			const el = document.createElement("input");
			//const { maxLength } = props.columnInfo.editor.options;
			
			el.type = "date"; /* 250527 수정 */
			el.className = "input-date input-date--in-grid flex-1"; /* 250527 수정 */
			el.value = String(props.value);
			//el.maxLength = maxLength;
			
			this.el = el;
		}
		
		getElement() {
			return this.el;
		}
		
		getValue() {
			return this.el.value;
		}
		
		mounted() {
			this.el.select();
		}
	}
	
	// select LI-type
	class GridSelectLIType {
		constructor(props) {
			const el = document.createElement("div");
			el.className = "select-wrap";
			el.classList.add('select-wrap--in-grid');
			
			const inputEl = document.createElement("input");
			inputEl.type = "text";
			inputEl.placeholder = "선택하세요";
			inputEl.className = "select-input";
			inputEl.value = props.value;
			
			el.append(inputEl);
			
			const ulEl = document.createElement("ul");
			ulEl.className = "select-options";
			
			for(let i=0; i<props.columnInfo.editor.options.listItems.length; i++){
				const liEl = document.createElement("li");
				liEl.className = "select-option";
				liEl.setAttribute("data-value", props.columnInfo.editor.options.listItems[i].value);
				liEl.innerText = props.columnInfo.editor.options.listItems[i].text;
				
				liEl.addEventListener('click', () => {
					inputEl.value = liEl.textContent.trim();
					el.classList.remove('open');
				});
				
				ulEl.append(liEl);
			}
			el.append(ulEl);
			
			inputEl.addEventListener("click", () => {
				el.classList.toggle("open");
				const options = ulEl.querySelectorAll("li");
				const tempSpan = document.createElement("span");
				tempSpan.style.visibility = 'hidden';
				tempSpan.style.position = 'absolute';
				tempSpan.style.whiteSpace = 'nowrap';
				tempSpan.style.fontSize = window.getComputedStyle(inputEl).fontSize;
				document.body.appendChild(tempSpan);

				let maxWidth = el.offsetWidth;

				options.forEach(option => {
					tempSpan.textContent = option.textContent.trim();
					const width = tempSpan.offsetWidth + 40; // 패딩 보정
					if (width > maxWidth) {
						maxWidth = width;
					}
				});

				document.body.removeChild(tempSpan);

				el.style.width = maxWidth + 'px';
			});
      inputEl.click();
			
			// 외부 클릭 시 닫기
			document.addEventListener('click', e => {
				if (!el.contains(e.target)) {
					el.classList.remove('open');
				}
			});
			
			this.el = el;
		}
			
		getElement() {
			return this.el;
		}
		
		getValue() {
			return this.el.querySelector("input").value;
		}
		
		mounted() {
			this.el.click();
		}
	}
	
	class GridSelectCheckbox {
		constructor(props) {
			const multiple = props.columnInfo.editor.options.multiple != false;
			let tempSelection = [];
			
			const el = document.createElement("div");
			el.className = "select-wrap";
      el.classList.add('select-wrap--in-grid');
			el.setAttribute("data-multiselect", multiple);
			
			const inputEl = document.createElement("input");
			inputEl.type = "text";
			inputEl.placeholder = "선택하세요";
			inputEl.className = "select-input";
			inputEl.value = props.value;
			
			el.append(inputEl);
			
			const ulEl = document.createElement("ul");
			ulEl.className = "select-options";
			
			for(let i=0; i<props.columnInfo.editor.options.listItems.length; i++){
				const liEl = document.createElement("li");
				liEl.className = "select-option";
				
				// const labelEl = document.createElement("label");
				// const checkboxEl = document.createElement("input");
				// checkboxEl.classList.add("checkbox");
				// checkboxEl.type = "checkbox";
				// checkboxEl.value = props.columnInfo.editor.options.listItems[i].value;
				// checkboxEl.checked = props.value.split(", ").includes(checkboxEl.value);
				
        const labelEl = document.createElement("label");
				const checkboxEl = document.createElement("input");
				const iconEl = document.createElement("span");
				const txtEl = document.createElement("span");
				labelEl.classList.add("checkbox");
				checkboxEl.classList.add("checkbox__input");
				iconEl.classList.add("checkbox__icon");
				txtEl.classList.add("checkbox__txt");
				checkboxEl.type = "checkbox";
				checkboxEl.value = props.columnInfo.editor.options.listItems[i].value;
				checkboxEl.checked = props.value.split(", ").includes(checkboxEl.value);

				if(multiple){
					checkboxEl.addEventListener('change', () => {
						this.updateMultiSelectInput();
						this.syncSelectAllCheckbox();
					});
				} else {
					liEl.addEventListener('click', () => {
						inputEl.value = liEl.textContent.trim();
						el.classList.remove('open');
					});
				}
				// labelEl.append(checkboxEl);
				// labelEl.append(props.columnInfo.editor.options.listItems[i].text);
				// liEl.append(labelEl);
				// ulEl.append(liEl);
				labelEl.append(checkboxEl);
				labelEl.append(iconEl);
				labelEl.append(txtEl);
				txtEl.prepend(props.columnInfo.editor.options.listItems[i].text);
				liEl.append(labelEl);
				ulEl.append(liEl);
			}
			
			const buttonEl = document.createElement("li");
			buttonEl.className = "select-actions";
			
			// const buttonSpanEl1 = document.createElement("span");
			// const buttonLabelEl1 = document.createElement("label");
			// const buttonCheckboxEl1 = document.createElement("input");
			// buttonCheckboxEl1.type = "checkbox";
			// buttonCheckboxEl1.className = "select-all";
			
			// buttonLabelEl1.append(buttonCheckboxEl1);
			// buttonSpanEl1.append(buttonLabelEl1);
			// buttonEl.append(buttonSpanEl1);
			
      const buttonLabelEl = document.createElement("label");
      const buttonCheckboxEl = document.createElement("input");
      const buttonIconEl = document.createElement("span");
      buttonLabelEl.classList.add("checkbox");
      buttonCheckboxEl.classList.add("checkbox__input","select-all");
      buttonIconEl.classList.add("checkbox__icon");
      buttonCheckboxEl.type = "checkbox";

      buttonLabelEl.append(buttonCheckboxEl);
      buttonLabelEl.append(buttonIconEl);
      buttonEl.append(buttonLabelEl);

			const buttonSpanEl2 = document.createElement("span");
			const buttonButtonEl1 = document.createElement("button");
			buttonButtonEl1.className = "btn-sm btn--quarternary-outline";
			buttonButtonEl1.innerText = "취소";
			buttonButtonEl1.addEventListener('click', () => {
				el.querySelectorAll('.select-option input[type="checkbox"]').forEach((chk, i) => {
					chk.checked = tempSelection[i];
				});
				this.updateMultiSelectInput();
				this.syncSelectAllCheckbox();
				el.classList.remove('open');
			});
			const buttonButtonEl2 = document.createElement("button");
			buttonButtonEl2.className = "btn-sm btn--primary";
			buttonButtonEl2.innerText = "확인";
			buttonButtonEl2.addEventListener('click', () => {
				el.classList.remove('open');
			});
			
			buttonSpanEl2.append(buttonButtonEl1);
			buttonSpanEl2.append(buttonButtonEl2);
			buttonEl.append(buttonSpanEl2);
			
			ulEl.append(buttonEl);
			
			el.append(ulEl);
			
			inputEl.addEventListener("click", () => {
				el.classList.toggle("open");
				if(multiple) {
					tempSelection = Array.from(el.querySelectorAll('.select-option input[type="checkbox"]')).map(chk => chk.checked);
				}
				const options = ulEl.querySelectorAll("li");
				const tempSpan = document.createElement("span");
				tempSpan.style.visibility = 'hidden';
				tempSpan.style.position = 'absolute';
				tempSpan.style.whiteSpace = 'nowrap';
				tempSpan.style.fontSize = window.getComputedStyle(inputEl).fontSize;
				document.body.appendChild(tempSpan);

				let maxWidth = el.offsetWidth;

				options.forEach(option => {
					tempSpan.textContent = option.textContent.trim();
					const width = tempSpan.offsetWidth + 40; // 패딩 보정
					if (width > maxWidth) {
						maxWidth = width;
					}
				});

				document.body.removeChild(tempSpan);

				el.style.width = maxWidth + "px";
			});
      inputEl.click();
			
			  // 전체 선택/해제 동작
			if(multiple) {
				buttonCheckboxEl.addEventListener("change", () => {
					el.querySelectorAll(".select-option input[type='checkbox']").forEach(chk => chk.checked = buttonCheckboxEl.checked);
					this.updateMultiSelectInput();
				});
			}
			
			// 외부 클릭 시 닫기
			document.addEventListener("click", e => {
				if (!el.contains(e.target)) {
					el.classList.remove("open");
				}
			});
			
			this.el = el;
			
			this.syncSelectAllCheckbox();
		}
			
		getElement() {
			return this.el;
		}
		
		getValue() {
			return this.el.querySelector("input").value;
		}
		
		mounted() {
			this.el.click();
		}
		
		updateMultiSelectInput() {
			const checkedBoxes = this.el.querySelectorAll(".select-option input[type='checkbox']:checked");

			if (checkedBoxes.length === 0) {
				this.el.querySelector(".select-input").value = "";
			} else {
				const selectedTexts = Array.from(checkedBoxes).map(chk => chk.parentElement.textContent.trim());
				this.el.querySelector(".select-input").value = selectedTexts.join(", ");
			}
		}
		
		syncSelectAllCheckbox() {
			const selectAllCheckbox = this.el.querySelector(".select-all");
			const allCheckboxes = this.el.querySelectorAll(".select-option input[type='checkbox']");
			const checkedCount = this.el.querySelectorAll(".select-option input[type='checkbox']:checked").length;
			if (selectAllCheckbox) selectAllCheckbox.checked = allCheckboxes.length > 0 && allCheckboxes.length === checkedCount;
		}
	}

//   class GridCheckbox {
//   constructor(props) {
//     const el = document.createElement('input');
//     el.type = 'checkbox';
//     el.checked = props.value;
//     el.addEventListener('change', () => {
//       props.grid.setValue(props.rowKey, props.columnName, el.checked);
//     });
//     this.el = el;
//   }
//   getElement() {
//     return this.el;
//   }
//   render(props) {
//     this.el.checked = props.value;
//   }
// }




