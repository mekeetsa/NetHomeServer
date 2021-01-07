
///////////////////////////////////////////////////////////////////////////////
// fullscreen functionality

function toggleFullscreen() {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen();
  } else if (document.exitFullscreen) {
    document.exitFullscreen();
  }
}

///////////////////////////////////////////////////////////////////////////////
// Responsive topnav 

/* Toggle between adding and removing the "responsive" class to topnav when the user clicks on the icon */

function topnavFunction() {
  var el = document.getElementById("navbar").children[0];
  if (el.className === "navbar") {
    el.className += " responsive";
  } else {
    el.className = "navbar";
  }
} 

///////////////////////////////////////////////////////////////////////////////
// Keep navbar on scroll (in the case if we have a header above the navbar)

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

// PWA: Register service worker to control making site work offline
//
if('serviceWorker' in navigator) {
  navigator.serviceWorker
    .register('/web/home/serviceWorker.js',{scope: '/'})
    .then(function(registration) { 
      console.log('ServiceWorker registration successful with scope:',  registration.scope);
    });
}

