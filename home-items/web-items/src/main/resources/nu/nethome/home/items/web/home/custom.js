
///////////////////////////////////////////////////////////////////////////////
// fullscreen functionality

function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen();
    document.getElementById("navbar-space").style.height = '4em'
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen();
      document.getElementById("navbar-space").style.height = '2em'
    }
  }
}

document.addEventListener("keypress", function(e) {
  if (e.keyCode == 'F'.charCodeAt(0) || e.keyCode == 'f'.charCodeAt(0)) {
    toggleFullscreen();
  }
}, false);

///////////////////////////////////////////////////////////////////////////////
// keep navbar on scroll

/*

function navbarScroller ()
{
// var el = document.getElementById("head");
// var nohead = ( window.getComputedStyle(el).display === "none" );
   var currentScrollPos = window.pageYOffset;
// if( nohead ) {
      document.getElementById("navbar").style.top = currentScrollPos > 0 ? "0px" : "" + (-currentScrollPos) + "px";
// } else {
//    document.getElementById("navbar").style.top = currentScrollPos > 145 ? "-145px" : "" + (-currentScrollPos) + "px";
// }
}

window.onscroll = navbarScroller;
navbarScroller ();  

*/
