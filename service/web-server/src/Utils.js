const { spawn } = require('child_process');

function displayStream(stream){
    stream.stdout.on('data', (data) => {
        console.log(`${data}`);
    });
      
    stream.stderr.on('data', (data) => {
        process.stdout.write(`${data}`);
    });
      
    stream.on('close', (code) => {
        if(code !== 0){
            console.log(`Script terminated with code ${code}`);
        }
    });
}
    
module.exports = {
    extractPRInfo(req){
        return {
            id : req.body.repository.id + req.body.pull_request.merge_commit_sha + Date.now(),
            commit: req.body.pull_request.merge_commit_sha,
            repoUrl: req.body.repository.html_url,
            repoId: req.body.repository.id,
            prMergedAt: req.body.pull_request.merged_at,
            prUrl: req.body.pull_request.html_url,
            prNumber: req.body.pull_request.number,
            executionStartedAt: '',
            executionCompletedAt: '',
            status: 'INITIALISING',
            vmName: 'benchmark-executor-'+ req.body.pull_request.merge_commit_sha
        }
    },
    startBenchmarking(scriptPath, execution){
        const ls = spawn('bash', [scriptPath, execution.repoUrl, execution.id, execution.commit, execution.vmName])
        displayStream(ls);  
    },
    deleteInstance(scriptPath, vmName){
        const ls = spawn('bash', [scriptPath, vmName])
        displayStream(ls);  
    }
}
