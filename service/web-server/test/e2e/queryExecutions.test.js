const request = require('supertest');
const elasticsearch = require('elasticsearch');
const ExecutionsController = require('../../src/ExecutionsController');
const app = require('../../src/server'); 
let esClient;
let execController;

beforeAll(() => {
    esClient = new elasticsearch.Client({host: 'localhost:9200'});
    execController = new ExecutionsController(esClient);
})

afterAll(() => {
    esClient.close();
})

beforeEach(()=>{
    return deleteAllExecutions();
})

describe('/execution/query tests', ()=>{

    test('When asking for INITIALISING execution, get only execution that is initialising', async (done) => {

        await addInitialisingExecution();
        await addCompletedExecution();

        request(app).get('/execution/query')
        .query({ query: '{ executions(status: ["INITIALISING"]) { commit status } }' })
        .expect(200)
        .then((res)=>{
            expect(res.body.data.executions).toHaveLength(1);
            done();
        });
    });

    test('When asking for execution without specifing status, get all executions', async (done) => {

        await addInitialisingExecution();
        await addCompletedExecution();

        request(app).get('/execution/query')
        .query({ query: '{ executions { commit status } }' })
        .expect(200)
        .then((res)=>{
            expect(res.body.data.executions).toHaveLength(2);
            done();
        });
    });

    test.only('When asking for execution specifying multiple status, get all executions that have one of the listed status', async (done) => {

        // const ciao = await addInitialisingExecution();
        
        // await addCompletedExecution();
        request(app).get('/execution/query')
        .query({ query: '{ executions(status: ["INITIALISING", "COMPLETED"], orderBy: "prMergedAt") { id commit status prMergedAt} }' })
        .expect(200)
        .then((res)=>{
            expect(res.body.data.executions).toHaveLength(2);
            done();
        });
    });

    test('When asking for spans, get all spans', async (done) => {

        
        request(app).get('/span/query')
        .query({ query: `{ querySpans(
                                limit: 100, 
                                graphName: "societal_model", 
                                executionName: "1234e7de3586be3c05068ad3a9119019b8009ca5f2f81548760002642"
                            ){ 
                                name 
                                duration 
                                tags { graphName executionName }} }` })
        .expect(200)
        .then((res)=>{
            expect(res.body.data.executions).toHaveLength(2);
            done();
        });
    });
  });




  // HELPERS



function deleteAllExecutions(){
    // return esClient.deleteByQuery({ 
    //     index: 'grakn-benchmark', 
    //     type: 'execution', 
    //     body: { query: { "match_all": {} } }
    // });
}

function addInitialisingExecution(){
    const executionObject = {
        commit: "48e57e1c848a5a2d61dc8d3e1b4e6a8d2a3d8f22",
        executionCompletedAt: "",
        prMergedAt: "4567",
        prNumber: "2",
        prUrl: "http://test.com/pulls/2",
        repoId: "135493233",
        repoUrl: "http://test.com",
        executionStartedAt: "",
        status: "INITIALISING",
        vmName: "benchmark-executor-1234",
      };
    return execController.addExecution(executionObject);
}

async function addCompletedExecution(){
    const executionObject = {
        commit: "12345",
        executionCompletedAt: "",
        prMergedAt: "4567",
        prNumber: "2",
        prUrl: "http://test.com/pulls/2",
        repoId: "09875",
        repoUrl: "http://test.com",
        executionStartedAt: "",
        status: "INITIALISING",
        vmName: "benchmark-executor-12345",
      };
    await execController.addExecution(executionObject);
    return execController.executionCompleted(executionObject);
}