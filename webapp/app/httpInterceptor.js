

myApp.factory("httpInterceptor", function ($localStorage) {

  return {

    request: function (config) {

      if (!angular.isUndefined($localStorage.cre)
          && !angular.isUndefined($localStorage.cre.access_token)) {
        config.headers["Authorization"] = $localStorage.cre.token_type + ' ' + $localStorage.cre.access_token;
        config.headers["Content-Type"] = 'application/json';
      }

      if (config.params && config.params['request'] === 'FILE_UPLOAD' || config.url.indexOf('?request=FILE_UPLOAD') > -1) {
        config.headers["Content-Type"] = undefined;
        // config.headers["Accept"] = undefined;
      }
      return config;
    }

  };
});
