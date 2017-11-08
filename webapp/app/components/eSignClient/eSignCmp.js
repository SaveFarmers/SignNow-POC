myApp.component('eSignCmp', {
  templateUrl: 'components/eSignClient/eSignCmp.html',
  controllerAs: 'ctrl',
  /* @ngInject */ //This is for Inline Array Annotation for Dependency Injection - Required when minification is done
  controller: function (APIService, $localStorage, upload, $q, $http, $filter, $timeout, $sce, $window) {

    var ctrl = this;

    angular.extend(this, {
      user: {
        email: '',
        password: ''
      },
      allDocuments: [],
      currentDocument: undefined,
      recipients: [],
      loggedIn: false,
      fields: [],
      htmlFields: [],
      storage: $localStorage
    });

    ctrl.$onInit = function() {
      ctrl.ping();
    };

    ctrl.ping = function() {
      APIService.eSign.pingServlet({request: 'PING'}).$promise.then(function(response) {
        console.log(response);
        if (ctrl.storage.cre) {
          ctrl.getAllDocuments();
        }
      });
    };

    ctrl.getOAuthToken = function() {
      var requestObj = {
        data: ctrl.user
      };
      ctrl.isLoading = true;
      APIService.eSign.getOAuthToken({request: 'GET_OAUTH_TOKEN'}, requestObj).$promise.then(function(response) {
        ctrl.isLoading = false;
        console.log(response);
        $localStorage.cre = response;
        $localStorage.email = ctrl.user.email;
        ctrl.loggedIn = true;
        ctrl.getAllDocuments();
      });
    };

    ctrl.uploadFile = function() {

      var baseURL = 'http://localhost:8080/SignNow/eSign';
      ctrl.isLoading = true;
      upload({
        url: baseURL + '?request=FILE_UPLOAD',
        method: 'POST',
        data: {
          file: ctrl.inputFile
        }

      }).then(
          function (response) {
            ctrl.isLoading = false;
            if (response.data) {
              angular.element("input[type='file']").val(null);
              setStatus({
                success: true,
                message: 'File is uploaded successfully...'
              });
              ctrl.getAllDocuments();
            }
          });
    };

    function setStatus(status) {
      ctrl.status = status;
      $timeout(function() {
        ctrl.status = undefined;
      }, 15000)
    }
    ctrl.getAllDocuments = function() {

      ctrl.isLoading = true;
      APIService.eSign.getAllDocuments({request: 'GET_ALL_DOCUMENTS'}).$promise.then(function(response) {
        ctrl.isLoading = false;
        if (response && response.length > 0) {
          response = $filter('orderBy')(response, 'created', true);
        }
        ctrl.allDocuments = response;
        if (ctrl.allDocuments.length > 0) {
          ctrl.getDocument(ctrl.allDocuments[0]);
        }
        for (var i = 0; i<ctrl.allDocuments.length; i++) {
          var obj = ctrl.allDocuments[i];
          // setImage(obj);
          obj.image = obj.thumbnail.small + "&access_token=" + $localStorage.cre.access_token;
          obj.displayDate =toDateTime(obj.created);
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
        ctrl.loadCurrentPage(ctrl.currentDocument.pages[0], 0);
        ctrl.getDocumentHistory(ctrl.currentDocument, true);
      });
    };

    ctrl.updateDocument = function() {

     /* var doc3 = {
        "fields": [
          {
            "required": true,
            "height": 15,
            "width": 200,
            "page_number": 0,
            "y": 500,
            "x": 150,
            "label": "Full Signature",
            "role": "signer1",
            "type": "signature"
          },
          {
            "required": true,
            "height": 15,
            "width": 200,
            "page_number": 0,
            "y": 600,
            "x": 150,
            "label": "Full Signature",
            "role": "signer2",
            "type": "signature"
          },
          {
            "x": 409,
            "y": 67,
            "width": 41,
            "height": 26,
            "page_number": 0,
            "role": "signer1",
            "label": "Initials",
            "required": true,
            "type": "initials"
          },
          {
            "x": 409,
            "y": 167,
            "width": 41,
            "height": 26,
            "page_number": 0,
            "role": "signer2",
            "label": "Initials",
            "required": true,
            "type": "initials"
          }
        ]
      };

      var doc1 = {
        "texts":[
          {
            "size":8,
            "x":61,
            "y":72,
            "page_number":0,
            "font":"Arial",
            "data":"a sample text field",
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
            "required": true,
            "height": 15,
            "width": 200,
            "page_number": 0,
            "y": 500,
            "x": 150,
            "label": "Some Stuff",
            "role":"signer1",
            "type":"signature"
          },
          {
            "x":307,
            "y":67,
            "width":60,
            "height":12,
            "page_number":0,
            "label":"a sample label",
            "role":"signer1",
            "required":true,
            "type":"text"
          },
          {
            "x":409,
            "y":67,
            "width":41,
            "height":26,
            "page_number":0,
            "role":"signer1",
            "required":true,
            "type":"initials"
          },
          {
            "x":481,
            "y":69,
            "width":12,
            "height":12,
            "page_number":0,
            "role":"CEO",
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
            "name": "GROUP_NAME",
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
          }
        ]
      }

      var doc2 = {
        "checks":[
          {
            "width":12,
            "height":12,
            "x":234,
            "y":53,
            "page_number":0
          }
        ]
      };*/

     if (ctrl.fields.length > 0) {
       APIService.eSign.updateDocument({
         request: 'UPDATE_DOCUMENT',
         documentId: ctrl.currentDocument.id
       }, {document: {fields: ctrl.fields}}).$promise.then(function (response) {
         setStatus({
           success: true,
           message: 'Document is updated successfully...'
         });
         console.log(response);
         ctrl.getAllDocuments();
         ctrl.getDocument(ctrl.currentDocument);
       });
     }
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
      ctrl.isLoading = true;
      APIService.eSign.deleteDocument({request: 'DELETE_DOCUMENT', documentId: doc.id}, {}).$promise.then(function(resposne) {
        ctrl.isLoading = false;
        setStatus({
          success: true,
          message: 'File is deleted successfully...'
        });
        ctrl.getAllDocuments();
      })
    };

    ctrl.sendFreeFormInvite = function(doc, recipients) {
      ctrl.isLoading = true;
      APIService.eSign.invite({request: 'SEND_INVITE'}, {documentId: doc.id, involvedParties: {'from': $localStorage.email, 'to': recipients}}).$promise.then(function(response) {
        ctrl.isLoading = false;
        setStatus({
          success: true,
          message: 'Free Form Invite has been sent "' + recipients + '" successfully...'
        });
        ctrl.getAllDocuments();
      })
    };

    // Convert the buffer to base64
    var _arrayBufferToBase64 = function( buffer ) {
      var binary = '';
      var bytes = new Uint8Array( buffer );
      var len = bytes.byteLength;
      // console.log(len);
      for (var i = 0; i < len; i++) {
        binary += String.fromCharCode( bytes[ i ] );
      }
      return window.btoa( binary );
    };

    ctrl.openRolesBasedInvite = function(doc) {
      ctrl.invite = {
        from: $localStorage.email,
        subject: "'" + $localStorage.email + "' Needs Your Signature'",
        message: "'" + $localStorage.email + "' invited you to sign '" + doc.original_filename + "'.",
        recipients: []
      };
      ctrl.addInvitee();
      ctrl.collectInviteDetailsFlag = true;
      $timeout(function () {
        $("#inviteDetails").modal();
      });
    };

    ctrl.addInvitee = function() {
      ctrl.invite.recipients.push({role: {}, email: '', order: 1});
    }

    ctrl.sendRoleBasedInvite = function () {
      ctrl.isLoading = true;
      var inviteObj = {
        from: ctrl.invite.from,
        subject: ctrl.invite.subject,
        message: ctrl.invite.message,
        to: []
      };

      for (var i = 0; i<ctrl.invite.recipients.length; i++) {
        var obj = ctrl.invite.recipients[i];
        inviteObj.to.push({
          email: obj.email,
          role_id: obj.role.unique_id,
          role: obj.role.name,
          order: obj.order
        })
      }
      ctrl.collectInviteDetailsFlag = false;
      APIService.eSign.invite({request: 'SEND_INVITE'}, {documentId: ctrl.currentDocument.id, involvedParties: inviteObj}).$promise.then(function(response) {
        ctrl.isLoading = false;
        setStatus({
          success: true,
          message: 'Role Based Invite has been sent successfully...'
        });
        console.log(response);
        ctrl.collectRoleBasedInvitationDetails = false;
        ctrl.invite = {};
        ctrl.getAllDocuments();
      });
    }

    ctrl.collectFieldDetails = function (ev) {
      ctrl.fields.push({
        x: ev.offsetX,
        y: ev.offsetY,
        page_number: ctrl.currentPage.pageIndex ? ctrl.currentPage.pageIndex : 0,
        width: 200,
        height: 20,
        required: true,
        type: 'signature',
        role: 'Signer1',
        label: 'Full Signature'
      });
      ctrl.currentField = ctrl.fields[ctrl.fields.length - 1];
      ctrl.currentFieldType = 'DOCUMENT';
      ctrl.collectFieldDetailsFlag = true;
      $timeout(function () {
        $("#fieldDetails").modal();
      });
    };

    ctrl.collectHTMLFieldDetails = function (ev) {
      ctrl.htmlFields.push({
        x: ev.target.offsetLeft,
        y: ev.target.offsetTop,
        targetElementId: ev.target.id,
        page_number: 0,
        width: 200,
        height: 20,
        required: true,
        type: 'signature',
        role: 'Signer1',
        label: 'Full Signature'
      });

      console.log('x: ' + ev.target.offsetLeft + ", y: " + ev.target.offsetTop);
      var css = "@element '#" + ev.target.id + "'{.htmlField_" + (ctrl.htmlFields.length - 1) + "{ top: eval('offsetTop')px; left: eval('offsetLeft')px;}}",
          head = document.head;

      var style;
      //var x = document.getElementsByTagName("STYLE");
      style = document.createElement('style');
      /*if (!x) {
        style = document.createElement('style');
      } else {
        style = x[0];
      }*/

      style.type = 'text/css';
      style.id = 'eSign-style';
      if (style.styleSheet){
        style.styleSheet.cssText = css;
      } else {
        style.appendChild(document.createTextNode(css));
      }

      head.appendChild(style);
      $timeout(function() {
        var nullString = undefined;
        style.removeAttribute('data-eqcss-read');
        EQCSS.load();
      });

      ctrl.currentField = ctrl.htmlFields[ctrl.htmlFields.length - 1];
      ctrl.currentFieldType = 'HTML';
      ctrl.collectFieldDetailsFlag = true;
      $timeout(function() {
        $("#fieldDetails").modal();
      });
    };

    ctrl.openFieldDetails = function(field) {
      ctrl.currentField = field;
      ctrl.collectFieldDetailsFlag = true;
      $timeout(function() {
        $("#fieldDetails").modal();
      });    };

    ctrl.updateFieldValues = function() {

      switch(ctrl.currentField.type) {
        case 'signature':
          ctrl.currentField.label = 'Full Signature';
          ctrl.currentField.width = 200;
          break;
        case 'text':
          ctrl.currentField.label = 'Your Address';
          ctrl.currentField.width = 400;
          break;
        case 'initials':
          ctrl.currentField.label = 'Initials';
          ctrl.currentField.width = 40;
          break;
        case 'checkbox':
          ctrl.currentField.label = 'I Agree the Terms and Conditions';
          ctrl.currentField.width = 20;
          break;
      }
    };

    ctrl.closeModal = function(rollback) {

      if (rollback) {
        if (ctrl.currentFieldType === 'DOCUMENT') {
          ctrl.fields.pop();
          for (var i = 0; i<ctrl.fields.length; i++) {
            var obj = ctrl.fields[i];
            console.log(obj);
          }
        } else {
          ctrl.htmlFields.pop();
          for (var i = 0; i<ctrl.htmlFields.length; i++) {
            var obj = ctrl.htmlFields[i];
            console.log(obj);
          }
        }
      }
      $('#fieldDetails').modal('hide');
      $(".modal-backdrop").remove();
      ctrl.collectFieldDetailsFlag = false;
    };

    ctrl.getDocumentHistory = function(doc, dontLoadDocument) {
      ctrl.isLoading = true;
      if (!dontLoadDocument) {
        ctrl.getDocument(doc);
      }
      APIService.eSign.getDocumentHistory({request: 'GET_DOCUMENT_HISTORY', documentId: doc.id}).$promise.then(function(response) {
        ctrl.isLoading = false;
        ctrl.history = response;
        console.log(ctrl.history);
        for (var i = 0; i<ctrl.history.length; i++) {
          var obj = ctrl.history[i];
          obj.displayDate =toDateTime(obj.created);
        }
      });
    };

    ctrl.createTemplate = function(doc) {
      ctrl.isLoading = true;
      APIService.eSign.createTemplate({request: 'CONVERT_INTO_TEMPLATE'}, {document_name: doc.original_filename, document_id: doc.id}).$promise.then(function(response) {
        ctrl.isLoading = false;
        console.log(response);
        setStatus({
          success: true,
          message: 'Created a new template...'
        });
        ctrl.getAllDocuments();
      });
    };

    ctrl.createDocumentFromTemplate = function(doc) {
      ctrl.isLoading = true;
      var date = new Date();
      var dateStr = date.getDate() + "" + (date.getMonth() + 1) + date.getFullYear() + "_" + date.getHours() + date.getMinutes();
      APIService.eSign.createDocumentFromTemplate({request: 'CREATE_DOCUMENT_FROM_TEMPLATE', templateId: doc.id}, {document_name: doc.original_filename + "_" + dateStr}).$promise.then(function(response) {
        ctrl.isLoading = false;
        console.log(response);
        setStatus({
          success: true,
          message: 'Created a new document from the template...'
        });
        ctrl.getAllDocuments();
      });
    };

    ctrl.logout = function() {
      ctrl.storage.$reset();
    }

    ctrl.getHtmlContent = function() {
      ctrl.htmlContent = '';
      $http.get('sampleContract.html').then(function(response) {
        ctrl.htmlContent = $sce.trustAsHtml(response.data);
        $timeout(function() {
          addListeners();
        })
      });
    };

    ctrl.releaseElementBinding = function() {
      /*var style = document.createElement('style');
      style.type = 'text/css';
      style.innerHTML = '.cssClass { color: #F00; }';
      document.getElementsByTagName('head')[0].appendChild(style);

      document.getElementById('someElementId').className = 'cssClass';*/

      for (var i = 0; i<ctrl.htmlFields.length; i++) {
        var field = ctrl.htmlFields[i];
        var element = document.getElementById('htmlField_' + i);
        var style = window.getComputedStyle(element);
        var top = style.getPropertyValue('top');
        var left = style.getPropertyValue('left');
        element.style.top = top;
        element.style.left = left;
        field.x = left;
        field.y = top;
      }

      ctrl.releaseBinding = true;

    };

    $window.allowDrop = function (ev) {
      console.log('ctrl.allowDrop');
      ev.preventDefault();
    }

    $window.drag = function (ev) {
      console.log('ctrl.drag');
      ev.dataTransfer.setData("text", ev.target.id);
    }

    $window.drop = function (ev) {
      console.log('ctrl.drop');
      ev.preventDefault();
      var data = ev.dataTransfer.getData("text");
      // ev.target.appendChild(document.getElementById(data));
      document.getElementById(data).style.top = ev.target.offsetTop + 'px';
      document.getElementById(data).style.left = ev.target.offsetLeft + 'px';
    }

    ctrl.replaceDynamicContent = function() {
      document.getElementById('instruct17').innerText = "This Agreement constitutes the entire agreement of the parties relating to the subject matter addressed in this Agreement. This Agreement supersedes all prior communications, contracts, or agreements between the parties with respect to the subject matter addressed in this Agreement, whether oral or written.This Agreement constitutes the entire agreement of the parties relating to the subject matter addressed in this Agreement. This Agreement supersedes all prior communications, contracts, or agreements between the parties with respect to the subject matter addressed in this Agreement, whether oral or written.This Agreement constitutes the entire agreement of the parties relating to the subject matter addressed in this Agreement. This Agreement supersedes all prior communications, contracts, or agreements between the parties with respect to the subject matter addressed in this Agreement, whether oral or written.This Agreement constitutes the entire agreement of the parties relating to the subject matter addressed in this Agreement. This Agreement supersedes all prior communications, contracts, or agreements between the parties with respect to the subject matter addressed in this Agreement, whether oral or written.This Agreement constitutes the entire agreement of the parties relating to the subject matter addressed in this Agreement. This Agreement supersedes all prior communications, contracts, or agreements between the parties with respect to the subject matter addressed in this Agreement, whether oral or written.This Agreement constitutes the entire agreement of the parties relating to the subject matter addressed in this Agreement. This Agreement supersedes all prior communications, contracts, or agreements between the parties with respect to the subject matter addressed in this Agreement, whether oral or written."

      document.getElementById('instruct16').innerText = "It was agreed that Ngomme & Segwagwa Attorneys should pay a total service fee of P28000 over three (3) Months’ time frame with the first P7000 (Seven Thousand Pula) payable on the day of the signing of this agreement and the other installments of P7000 (Seven Thousand Pula) being payable on the 5th of every month. Failure to pay the monthly installments on the agreed dates will result in the balance amount to accrue an interest of 30% per month and that the collection all the fees and costs that Delswiz Management Consultant (Pty) Ltd, may incur in collection of my balance owed as well as a competitive interest rate to be added to the total amount owed.\nIt was agreed that Ngomme & Segwagwa Attorneys should pay a total service fee of P28000 over three (3) Months’ time frame with the first P7000 (Seven Thousand Pula) payable on the day of the signing of this agreement and the other installments of P7000 (Seven Thousand Pula) being payable on the 5th of every month. Failure to pay the monthly installments on the agreed dates will result in the balance amount to accrue an interest of 30% per month and that the collection all the fees and costs that Delswiz Management Consultant (Pty) Ltd, may incur in collection of my balance owed as well as a competitive interest rate to be added to the total amount owed.\nIt was agreed that Ngomme & Segwagwa Attorneys should pay a total service fee of P28000 over three (3) Months’ time frame with the first P7000 (Seven Thousand Pula) payable on the day of the signing of this agreement and the other installments of P7000 (Seven Thousand Pula) being payable on the 5th of every month. Failure to pay the monthly installments on the agreed dates will result in the balance amount to accrue an interest of 30% per month and that the collection all the fees and costs that Delswiz Management Consultant (Pty) Ltd, may incur in collection of my balance owed as well as a competitive interest rate to be added to the total amount owed.\n"
    };

    function addListeners() {
      document.addEventListener('click', function(e) {
        e = e || window.event;
        var target = e.target || e.srcElement,
            text = target.textContent || target.innerText;
        // console.log(target); console.log(text);
        // var caretPosition = document.caretPositionFromPoint(e.target.offsetLeft, e.target.offsetTop);
        // console.log(caretPosition);
        console.log(document.elementFromPoint(e.target.offsetLeft, e.target.offsetTop));
        console.log(document.elementFromPoint(e.offsetX, e.offsetY));
        console.log(document.elementFromPoint(e.clientX, e.clientY));
        console.log(document.elementFromPoint(e.x, e.y));
        console.log(e.target);

      }, false);
    }

    function toDateTime(secs) {
      var date = new Date(secs * 1000);
      var date_string = date.toLocaleString('en-US');
      return date_string;
    }
  }
});