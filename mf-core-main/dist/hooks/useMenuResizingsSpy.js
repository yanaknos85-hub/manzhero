/**
 * Mutate dom element ant-message relatively open or closed menu. Unfortinatuly this element has absolute coords.
 */
export function useMenuResizingSpy() {
    var domMessage = document.querySelector('.ant-message-notice-content');
    var domSideMenu = document.querySelector('.side-menu-container');
    var sideMenuWidth = domSideMenu && getComputedStyle(domSideMenu).width;
    if (domMessage && domSideMenu && sideMenuWidth) {
        domMessage.style.marginLeft = sideMenuWidth;
    }
}
