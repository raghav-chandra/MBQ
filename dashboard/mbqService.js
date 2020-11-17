import { REDUX_ACTIONS, mbqStats, searchItems, changedStatus, queueItem } from './redux/actions';

const HTTP_GET = 'GET', HTTP_POST = 'POST';

const executeRequest = (dispatch, url, requestType, successAction, data) => {
      const createPromise = requestType =>{
          if(requestType === HTTP_GET) {
              return fetch(url, {credentials: 'include'});
          } else {
              let req = new Request(url, {
                  method:HTTP_POST,
                  credentials:'include',
                  headers: {'Content-Type' : 'application/json'},
                  body: data !=null && typeof data === 'object' ? JSON.stringify(data) : data
               });
               return fetch(req);
          }
      }

      return createPromise(requestType)
          .then(response=>{
              if(!response.ok) {
                  throw response.statusText;
              } else {
                  return response.json();
              }
          }).then (json=>{
              if(json.success) {
                  return dispatch (successAction(json.data));
              } else {
                  throw json.message;
              }
          }).catch(function(error) {
                  alert('Failed : ' + (error.message? error.message:error));
          });
  };

const executePostRequest = (dispatch, url, data, successAction) => executeRequest(dispatch, url, HTTP_POST, successAction, data);
const executeGetRequest = (dispatch, url, successAction) => executeRequest(dispatch, url, HTTP_GET, successAction);

const CALL_MAPPER = {
    [REDUX_ACTIONS.SEARCH_ITEMS] : (dispatch, data) => executePostRequest(dispatch, 'mbq/console/search', data, searchItems),
    [REDUX_ACTIONS.CHANGE_STATUS] : (dispatch, data) => executePostRequest(dispatch, 'mbq/console/updateStatus', data, (response) => alert ('Update was successful ' + response)),
    [REDUX_ACTIONS.GET_DATA] : (dispatch, id) => executeGetRequest(dispatch, 'mbq/console/get/'+id, queueItem),
}

export function execute(action, data = null) {
    return function (dispatch) {
        try {
            return CALL_MAPPER[action](dispatch, data);
        } catch (e) {
            alert ('Failed while executing ' + action + '. Please retry' +  e);
        }
    }
}

export const MBQService = {
    init: () => {},
    searchItems : request => execute (REDUX_ACTIONS.SEARCH_ITEMS, request),
    updateStatus : (ids, status) => execute (REDUX_ACTIONS.CHANGE_STATUS, {ids, status}),
    getData : id => execute (REDUX_ACTIONS.GET_DATA, id)
}