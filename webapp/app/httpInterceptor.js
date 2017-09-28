

myApp.factory("httpInterceptor", function ($localStorage) {

  return {

    request: function (config) {

      if (!angular.isUndefined($localStorage.cre)
          && !angular.isUndefined($localStorage.cre.access_token)) {
        config.headers["Authorization"] = $localStorage.cre.token_type + ' ' + $localStorage.cre.access_token;
        config.headers["Content-Type"] = 'application/json';
      }
      return config;
    }

  };
});
