console.log('非源码，仅用作演示。下载源码请访问：HTTP://WWW.bootstrapmb.com');console.log('非源码，仅用作演示。下载源码请访问：HTTP://WWW.bootstrapmb.com');const items = document.querySelectorAll(".accordion button");

function toggleAccordion() {
  const itemToggle = this.getAttribute('aria-expanded');
  
  for (i = 0; i < items.length; i++) {
    items[i].setAttribute('aria-expanded', 'false');
  }
  
  if (itemToggle == 'false') {
    this.setAttribute('aria-expanded', 'true');
  }
}

items.forEach(item => item.addEventListener('click', toggleAccordion));console.log('非源码，仅用作演示。下载源码请访问：HTTP://WWW.bootstrapmb.com');console.log('非源码，仅用作演示。下载源码请访问：HTTP://WWW.bootstrapmb.com');