import { REDUX_ACTIONS, mbqStats } from './redux/actions';

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
    [REDUX_ACTIONS.ALL_MBQ_STATS] : dispatch => executeGetRequest(dispatch, 'stats/all', mbqStats)
}

export function execute(action, param, data = null) {
    return function (dispatch) {
        try {
            return CALL_MAPPER[action](dispatch, param, data);
        } catch (e) {
            alert ('Failed while executing ' + action + '. Please retry' +  e);
        }
    }
}

export const MBQService = {
    getAllStats : () => execute (REDUX_ACTIONS.ALL_MBQ_STATS)
}