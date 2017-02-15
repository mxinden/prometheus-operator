job('e2e-tests') {
    scm {
        git {
            remote {
                github('mxinden/prometheus-operator')
                refspec('+refs/pull/*:refs/remotes/origin/pr/*')
            }
            branch('${sha1}')
        }
    }

    wrappers {
        credentialsBinding {
            amazonWebServicesCredentialsBinding{
                accessKeyVariable('AWS_ACCESS_KEY_ID')
                secretKeyVariable('AWS_SECRET_ACCESS_KEY')
                credentialsId('Jenkins-Monitoring-AWS-User')
            }
        }
    }

    triggers {
        githubPullRequest {
           onlyTriggerPhrase()
           useGitHubHooks()
        }
    }

    steps {
        shell('docker build -t cluster-setup-env test/jenkins-setup/.')
    }
    steps {
        shell('docker run --privileged --rm -e AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY -v $PWD:/go/src/github.com/coreos/prometheus-operator cluster-setup-env /bin/bash -c "cd /go/src/github.com/coreos/prometheus-operator/test/jenkins-setup && make"')
    }
    publishers {
        postBuildScripts {
            steps {
                shell('docker run --privileged --rm -e AWS_ACCESS_KEY_ID -e AWS_SECRET_ACCESS_KEY -v $PWD:/go/src/github.com/coreos/prometheus-operator cluster-setup-env /bin/bash -c "cd /go/src/github.com/coreos/prometheus-operator/test/jenkins-setup && make clean"')
            }
            onlyIfBuildSucceeds(false)
            onlyIfBuildFails(false)
        }
    }
}
