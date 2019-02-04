function buildQueriesMap(queries) {
  let matchQuery = 0;
  let matchInsertQuery = 0;
  let insertQuery = 0;
  let computeQuery = 0;
  let otherQuery = 0;
  const queriesMap = {};
  queries.forEach(query => {
    let value;
    if (query.includes("compute")) {
      value = `computeQuery${++computeQuery}`;
    } else if (query.includes("insert") && query.includes("match")) {
      value = `matchInsertQuery${++matchInsertQuery}`;
    } else if (query.includes("insert")) {
      value = `insertQuery${++insertQuery}`;
    } else if (query.includes("match")) {
      value = `matchQuery${++matchQuery}`;
    } else {
      value = `query${++otherQuery}`;
    }
    queriesMap[query] = value;
  });
  return queriesMap;
}

/**
 * queriesTime structure => [
 *                             {
 *                              query: "",
 *                              times: [ { commit: "", avgTime: "" }, ... ]
 *                              },
 *                              ...
 *                           ]
 */
function buildQueriesTimes(queries, spans, executions) {
  return queries.map(query => {
    // Find all the spans related to the current query
    const querySpans = spans.filter(span => span.tags.query === query);
    // For each commit, compute the average time the current query took to execute
    const times = executions.map(exec => {
      // Collect all the spans relative to this current commit and query
      const executionQuerySpans = querySpans.filter(
        span => span.tags.executionName === exec.id
      );
      // Compute average time combining all the repetitions
      const avgTimeMicro =
        executionQuerySpans.reduce((a, b) => a + b.duration, 0) /
        executionQuerySpans.length;
      const stdDeviation = stdDeviationFn(
        executionQuerySpans.map(x => x.duration),
        avgTimeMicro
      );
      if (stdDeviation > 60000) {
        console.log(
          "Numbers: " + executionQuerySpans.map(x => x.duration / 1000)
        );
        console.log("AvgTime: " + avgTimeMicro / 1000);
        console.log("stdDeviation: " + Number(stdDeviation / 1000).toFixed(3));
      }
      return {
        commit: exec.commit,
        avgTime: avgTimeMicro / 1000,
        stdDeviation: stdDeviation / 1000
      };
    });
    return { query, times };
  });
}
function stdDeviationFn(numbersArr, avgTime) {
  let SDprep = 0;
  for (const key in numbersArr)
    SDprep += Math.pow(parseFloat(numbersArr[key]) - avgTime, 2);
  return Math.sqrt(SDprep / numbersArr.length);
}

export default {
  buildQueriesMap,
  buildQueriesTimes
};
