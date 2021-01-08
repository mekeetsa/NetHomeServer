
self.addEventListener('install', function(e) {
 e.waitUntil(
   caches.open('nethome-store').then(function(cache) {
     return cache.addAll([
       '/web/home/'
     ]);
   })
 );
});

self.addEventListener('fetch', function(e) {
  // console.log(e.request.url);
  e.respondWith(
    caches.match(e.request).then(function(response) {
      return response || fetch(e.request);
    })
  );
});
