function postData(url = ``, data = {}) {
  // Default options are marked with *
  return fetch(url, {
    method: "POST", // *GET, POST, PUT, DELETE, etc.
    mode: "cors", // no-cors, cors, *same-origin
    cache: "no-cache", // *default, no-cache, reload, force-cache, only-if-cached
    credentials: "same-origin", // include, *same-origin, omit
    headers: { "Content-Type": "application/json" },
    redirect: "follow", // manual, *follow, error
    referrer: "no-referrer", // no-referrer, *client
    body: JSON.stringify(data) // body data type must match "Content-Type" header
  }).then(response => response.json()); // parses response to JSON
}

function getSpans(query) {
  return postData("/span/query", { query });
}
function getExecutions(query) {
  return postData("/execution/query", { query });
}

export default {
  getExecutions,
  getSpans,
  getExecutionsSpans: executions =>
    Promise.all(
      executions.map(exec =>
        getSpans(
          `{ querySpans( limit: 300, executionName: "${
            exec.id
          }"){ name duration tags { graphName executionName query }} }`
        ).then(res => res.data.querySpans)
      )
    ).then(nestedSpans => nestedSpans.flatMap(x => x)),
  getLatestCompletedExecutions: number =>
    getExecutions(
      `{ executions(status: ["COMPLETED"], orderBy: "prMergedAt", order:"desc", limit: ${number}){ id commit prMergedAt} }`
    ).then(executions => executions.data.executions)
};
