
myApp.factory('APIService', function($resource) {

  var baseURL = 'http://localhost:8080/SignNow/eSign';

  return {
    eSign:

    $resource(baseURL, {}, {

      pingServlet: {
        method: 'GET'
      },

      getOAuthToken: {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        }
      },

      getAllDocuments: {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        },
        isArray: true

      },

      getDocument: {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        }
      },

      updateDocument: {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        }
      },

      getDocumentHistory: {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        isArray: true
      },

      thumbnails: {
        method: 'POST',
        headers: {
          'Content-Type': 'arraybuffer'
          // 'Accept': 'application/json'
        },
        responseType: 'arraybuffer'
      },

      deleteDocument: {
        method: 'POST'
      },

      invite: {
        method: 'POST'
      }
    })
  }
})