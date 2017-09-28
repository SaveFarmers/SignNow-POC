myApp.component('eSignCmp', {
  templateUrl: 'components/eSignClient/eSignCmp.html',
  controllerAs: 'ctrl',
  /* @ngInject */ //This is for Inline Array Annotation for Dependency Injection
  controller: function (APIService, $localStorage, upload, $q) {

    var ctrl = this;

    angular.extend(this, {
      user: {
        email: '',
        password: ''
      },
      allDocuments: [],
      currentDocument: {},
      recipients: [],
      loggedIn: false
    });

    ctrl.$onInit = function() {
      ctrl.ping();
    };

    ctrl.ping = function() {
      APIService.eSign.pingServlet({request: 'PING'}).$promise.then(function(response) {
        console.log(response);
      })
    };

    ctrl.getOAuthToken = function() {
      var requestObj = {
        data: ctrl.user
      };
      APIService.eSign.getOAuthToken({request: 'GET_OAUTH_TOKEN'}, requestObj).$promise.then(function(response) {
        console.log(response);
        $localStorage.cre = response;
        $localStorage.email = ctrl.user.email;
        ctrl.loggedIn = true;
        ctrl.getAllDocuments();
      });
    };

    ctrl.uploadFile = function() {

      var baseURL = 'http://localhost:8080/SignNow/eSign';

      upload({
        url: baseURL + '?request=FILE_UPLOAD',
        method: 'POST',
        data: {
          file: ctrl.inputFile
        }

      }).then(
          function (response) {
            if (response.data) {

            }
          });
    };

    ctrl.getAllDocuments = function() {

      APIService.eSign.getAllDocuments({request: 'GET_ALL_DOCUMENTS'}).$promise.then(function(response) {
        ctrl.allDocuments = response;
        for (var i = 0; i<ctrl.allDocuments.length; i++) {
          var obj = ctrl.allDocuments[i];
          // obj.image = ctrl.getThumbnails(obj.thumbnail.small);
          obj.image = "http://localhost:8080/SignNow/eSign?request=THUMBNAILS&requestUrl=" + obj.thumbnail.small;
        }
      });
    };

    ctrl.getDocument = function(doc) {
      APIService.eSign.getDocument({request: 'GET_DOCUMENT', documentId: doc.id}).$promise.then(function(response) {
        ctrl.currentDocument = response;
      });
    }

    ctrl.getThumbnails = function(url) {
      var deferred = $q.defer();
      APIService.eSign.thumbnails({request: 'THUMBNAILS', requestUrl: url}, {requestUrl: url}).$promise.then(function(response) {
        deferred.resolve(response);
      });
      return deferred.promise;
    }

    ctrl.deleteDocument = function(doc) {
      APIService.eSign.deleteDocument({request: 'DELETE_DOCUMENT', documentId: doc.id}, {}).$promise.then(function(resposne) {
        ctrl.getAllDocuments();
      })
    }

    ctrl.sendInvite = function(doc, recipients) {
      APIService.eSign.invite({request: 'SEND_INVITE'}, {documentId: doc.id, involvedParties: {'from': $localStorage.email, 'to': recipients}}).$promise.then(function(response) {
        ctrl.getAllDocuments();
      })
    }
  }
});