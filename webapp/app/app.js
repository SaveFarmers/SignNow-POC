'use strict';

// Declare app level module which depends on views, and components
var myApp = angular.module('myApp', [
 'ngResource', 'ngStorage', 'lr.upload', 'ngSanitize'
]);

myApp.config(function ($httpProvider) {
  $httpProvider.interceptors.push('httpInterceptor');
});

/*
myApp.config(['$locationProvider', '$routeProvider', function($locationProvider, $routeProvider) {
  $locationProvider.hashPrefix('!');

  $routeProvider.otherwise({redirectTo: '/view1'});
}]);
*/
