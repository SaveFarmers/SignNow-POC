myApp.directive('bindFile', [function () {
  return {
    require: "ngModel",
    restrict: 'A',
    link: function ($scope, el, attrs, ngModel) {
      el.bind('change', function (event) {
        ngModel.$setViewValue(event.target.files[0]);
        $scope.$apply();
      });

      el.bind('click', function () {
        el.val('');
      });
    }
  };
}]);
