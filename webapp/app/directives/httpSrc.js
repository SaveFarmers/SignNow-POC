myApp.directive('httpSrc', ['$http', function ($http) {
  return {
    // do not share scope with sibling img tags and parent
    // (prevent show same images on img tag)
    scope: {},
    link: function ($scope, elem, attrs) {
      function revokeObjectURL() {
        if ($scope.objectURL) {
          URL.revokeObjectURL($scope.objectURL);
        }
      }

      $scope.$watch('objectURL', function (objectURL) {
        elem.attr('src', objectURL);
      });

      $scope.$on('$destroy', function () {
        revokeObjectURL();
      });

      attrs.$observe('httpSrc', function (url) {
        revokeObjectURL();

        if(url && url.indexOf('data:') === 0) {
          $scope.objectURL = url;
        } else if(url) {
          $http.get(url, {
            responseType: 'arraybuffer',
            headers: {
              'accept': 'image/webp,image/*,*/*;q=0.8'
            }
          })
              .then(function (response) {
                var blob = new Blob(
                    [ response.data ],
                    { type: 'image/png' }
                );
                $scope.objectURL = URL.createObjectURL(blob);
              });
        }
      });
    }
  };
}]);

myApp.directive('authenticatedSrc', ['APIService', function (APIService) {
  var directive = {
    link: link,
    restrict: 'A'
  };
  return directive;
  function link(scope, element, attrs) {
    var requestConfig = {
      cache: 'false',
      responseType: 'blob'
    };
    APIService.eSign.thumbnails({request: 'THUMBNAILS', requestUrl: attrs.authenticatedSrc}, {requestUrl: attrs.authenticatedSrc}).$promise.then(function(response) {
      var reader = new window.FileReader();
      reader.readAsDataURL(response.data);
      reader.onloadend = function() {
        attrs.$set('src', reader.result);
      };
    });
  }
}]);
