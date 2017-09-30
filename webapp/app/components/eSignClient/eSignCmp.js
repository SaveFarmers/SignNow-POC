myApp.component('eSignCmp', {
  templateUrl: 'components/eSignClient/eSignCmp.html',
  controllerAs: 'ctrl',
  /* @ngInject */ //This is for Inline Array Annotation for Dependency Injection
  controller: function (APIService, $localStorage, upload, $q, $http, $filter) {

    var ctrl = this;

    angular.extend(this, {
      user: {
        email: '',
        password: ''
      },
      allDocuments: [],
      currentDocument: undefined,
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
          file: ctrl.inputFile,
          data: '[t:s;r:y;o:"CEO";w:100;h:15;]'
        }

      }).then(
          function (response) {
            if (response.data) {
              ctrl.getAllDocuments();
            }
          });
    };

    ctrl.getAllDocuments = function() {

      APIService.eSign.getAllDocuments({request: 'GET_ALL_DOCUMENTS'}).$promise.then(function(response) {
        if (response && response.length > 0) {
          response = $filter('orderBy')(response, 'original_filename');
        }
        ctrl.allDocuments = response;
        if (!ctrl.currentDocument) {
          ctrl.getDocument(ctrl.allDocuments[0]);
        }
        for (var i = 0; i<ctrl.allDocuments.length; i++) {
          var obj = ctrl.allDocuments[i];
          setImage(obj);
          obj.image = "http://localhost:8080/SignNow/eSign?request=THUMBNAILS&requestUrl=" + obj.thumbnail.small;
        }
      });
    };

    function setImage(obj) {
      ctrl.getThumbnails(obj.thumbnail.medium).then(function(response) {
        obj.receivedImage = response;
      });
    }

    ctrl.loadCurrentPage = function(page, pageIndex) {
      ctrl.getThumbnails(page.src).then(function(response) {
        ctrl.currentPage = page;
        ctrl.currentPage.receivedImage = response;
        ctrl.currentPage.pageIndex = pageIndex;
      });
    }
    ctrl.getDocument = function(doc) {
      APIService.eSign.getDocument({request: 'GET_DOCUMENT', documentId: doc.id}).$promise.then(function(response) {
        ctrl.currentDocument = response;
        ctrl.showDocument = true;
        ctrl.loadCurrentPage(ctrl.currentDocument.pages[0]);
      });
    };

    ctrl.updateDocument = function(doc) {
      /*var doc1 = {
        "texts":[
          {
            "size":8,
            "x":61,
            "y":72,
            "page_number":0,
            "font":"Arial",
            "data":"a sample text field",
            "line_height":9.075
          }
        ],
        "checks":[
          {
            "width":12,
            "height":12,
            "x":234,
            "y":53,
            "page_number":0
          }
        ],
        "fields":[
          {
            "x":305,
            "y":18,
            "width":122,
            "height":34,
            "page_number":0,
            "role":"tatonka",
            "required":true,
            "type":"signature"
          },
          {
            "x":307,
            "y":67,
            "width":60,
            "height":12,
            "page_number":0,
            "label":"a sample label",
            "role":"tatonka",
            "required":true,
            "type":"text"
          },
          {
            "x":409,
            "y":67,
            "width":41,
            "height":26,
            "page_number":0,
            "role":"tatonka",
            "required":true,
            "type":"initials"
          },
          {
            "x":481,
            "y":69,
            "width":12,
            "height":12,
            "page_number":0,
            "role":"tatonka",
            "required":true,
            "type":"checkbox"
          },
          {
            "x":38,
            "y":77,
            "width":87,
            "height":16,
            "page_number":0,
            "label":"Select Year",
            "role":"Client",
            "required":true,
            "custom_defined_option":false,
            "enumeration_options":[
              "2014",
              "2015",
              "2016"
            ],
            "type":"enumeration"
          },
          {
            "x": 395,
            "y": 372,
            "width": 177,
            "height": 50,
            "page_number": 0,
            "role": "signer",
            "required": true,
            "type": "radiobutton",
            "name": "GROUP_NAME"
          }],
        "radio": [
        {
          "page_number": "0",
          "x": "10",
          "y": "20",
          "width": "25",
          "height": "25",
          "checked": "0",
          "value": "apple",
          "created": "123456789"
        },
        {
          "page_number": "0",
          "x": "40",
          "y": "20",
          "width": "25",
          "height": "25",
          "checked": "0",
          "value": "cherry",
          "created": "123456789"
        }
      ]
      };*/

      var doc1 = {
        "checks":[
          {
            "width":12,
            "height":12,
            "x":234,
            "y":53,
            "page_number":0
          }
        ]
      };

      APIService.eSign.updateDocument({request: 'UPDATE_DOCUMENT', documentId: doc.id}, {document: doc1}).$promise.then(function(response) {
        console.log(response);
        ctrl.getDocument(doc);
      });
    }

    ctrl.getThumbnails = function(url) {
      var deferred = $q.defer();
      $http({
            method: 'POST',
            url: 'http://localhost:8080/SignNow/eSign?request=THUMBNAILS&requestUrl=' + url,
            responseType: 'arraybuffer',
            data: {requestUrl: url}
      }).then(
          function successCallback(response) {
            var image = 'data:image/png;base64,'+_arrayBufferToBase64(response.data);
            // return image;
            deferred.resolve(image);
          },
          function errorCallback(response) {
            console.error(response);
          }
      );
      return deferred.promise;

    };

    ctrl.deleteDocument = function(doc) {
      APIService.eSign.deleteDocument({request: 'DELETE_DOCUMENT', documentId: doc.id}, {}).$promise.then(function(resposne) {
        ctrl.getAllDocuments();
      })
    };

    ctrl.sendInvite = function(doc, recipients) {
      APIService.eSign.invite({request: 'SEND_INVITE'}, {documentId: doc.id, involvedParties: {'from': $localStorage.email, 'to': recipients}}).$promise.then(function(response) {
        ctrl.getAllDocuments();
      })
    }

    // Convert the buffer to base64
    var _arrayBufferToBase64 = function( buffer ) {
      var binary = '';
      var bytes = new Uint8Array( buffer );
      var len = bytes.byteLength;
      console.log(len);
      for (var i = 0; i < len; i++) {
        binary += String.fromCharCode( bytes[ i ] );
      }
      return window.btoa( binary );
    };
  }
});