'use strict';

// var multipleTestUploadForm = document.querySelector('#multipleTestUploadForm');
var multipleFileTestUploadInput = document.querySelector('#multipleFileTestUploadInput');
// var multipleFileTestUploadSuccess = document.querySelector('#multipleFileTestUploadSuccess');

var evaluateForm = document.querySelector('#evaluateForm');
var evaluationInput = document.querySelector('#evaluationInput');
var evaluationError = document.querySelector('#evaluationError');
var evaluationSuccess = document.querySelector('#evaluationSuccess');

/* 
 * This code was modified from
 * https://github.com/callicoder/spring-boot-file-upload-download-rest-api-example
 *
 */

function evaluate(ontologyKey, boundaryMatchStrategy, files) {
    var formData = new FormData();
    formData.append("bms", boundaryMatchStrategy);
    formData.append("ont", ontologyKey);
    for(var index = 0; index < files.length; index++) {
        formData.append("files", files[index]);
    }

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/eval"); 

    xhr.onload = function() {
        console.log(xhr.responseText);
        var response = JSON.parse(xhr.responseText);
        updateEvalButton("stop");
        if(xhr.status == 200) {
            evaluationError.style.display = "none";
            evaluationSuccess.innerHTML = "<h3>Results:</h3> <ul><li><b>matches:</b> " + response.matches 
                                                        + "</li><li><b>insertions:</b> " + response.insertions
                                                        + "</li><li><b>deletions:</b> " + response.deletions
                                                        + "</li><li><b>predicted count:</b> " + response.predictedCount
                                                        + "</li><li><b>reference count:</b> " + response.referenceCount
                                                        + "</li><li><b>slot error rate:</b> " + response.slotErrorRate
                                                        + "</li><li><b>precision:</b> " + response.precision
                                                        + "</li><li><b>recall:</b> " + response.recall
                                                        + "</li><li><b>F1-score:</b> " + response.fscore 
                                                        + "</li></ul>";
            evaluationSuccess.style.display = "block";
        } else {
            evaluationSuccess.style.display = "none";
            evaluationError.innerHTML = (response && response.message) || "Some Error Occurred";
        }
    }
    xhr.send(formData);
}

// function uploadSingleFile(file, type) {
//     var formData = new FormData();
//     formData.append("file", file);
//     formData.append("type", type);

//     var xhr = new XMLHttpRequest();
//     xhr.open("POST", "/uploadFile");

//     xhr.onload = function() {
//         console.log(xhr.responseText);
//         var response = JSON.parse(xhr.responseText);
//         if(xhr.status == 200) {
//             singleFileUploadError.style.display = "none";
//             singleFileUploadSuccess.innerHTML = "<p>File Uploaded Successfully.</p><ul><li>" + response.fileName + "</li></ul>";
//             singleFileUploadSuccess.style.display = "block";
//         } else {
//             singleFileUploadSuccess.style.display = "none";
//             singleFileUploadError.innerHTML = (response && response.message) || "Some Error Occurred";
//         }
//     }

//     xhr.send(formData);
// }

// function uploadMultipleFiles(files, type) {
//     var formData = new FormData();
//     for(var index = 0; index < files.length; index++) {
//         formData.append("files", files[index]);
//     }
//     formData.append("type", type);

//     var xhr = new XMLHttpRequest();
//     xhr.open("POST", "/uploadMultipleFiles");

//     xhr.onload = function() {
//         console.log(xhr.responseText);
//         var response = JSON.parse(xhr.responseText);
//         var multipleFileUploadSuccess;
//         if(type == "REF") {
//            multipleFileUploadSuccess = multipleFileRefUploadSuccess;
//         } else if (type == "TEST") {
//            multipleFileUploadSuccess = multipleFileTestUploadSuccess;
//         } else {
//            multipleFileUploadSuccess = multipleFileTxtUploadSuccess;
//         }
//         if(xhr.status == 200) {
//             multipleFileUploadError.style.display = "none";
//             var content = "<p>All Files Uploaded Successfully</p>";
//             //content += "<ul>";
//             //for(var i = 0; i < response.length; i++) {
//             //    content += "<li>" + response[i].fileName + "</li>";
//             //}
//             content += "</ul>";
//             multipleFileUploadSuccess.innerHTML = content;
//             multipleFileUploadSuccess.style.display = "block";
//         } else {
//             multipleFileUploadSuccess.style.display = "none";
//             multipleFileUploadError.innerHTML = (response && response.message) || "Some Error Occurred";
//         }
//     }

//     xhr.send(formData);
// }

function updateEvalButton(status) {
  if (status == "run") {
    document.getElementById("evalButton").style.backgroundColor = "#01ee01";
  } else {
    document.getElementById("evalButton").style.backgroundColor = "#128ff2";
  }
}

// multipleTestUploadForm.addEventListener('submit', function(event){
//     var files = multipleFileTestUploadInput.files;
//     if(files.length === 0) {
//         multipleFileUploadError.innerHTML = "Please select at least one file";
//         multipleFileUploadError.style.display = "block";
//     }
//     uploadMultipleFiles(files, "TEST");
//     event.preventDefault();
// }, true);

evaluateForm.addEventListener('submit', function(event){
    var boundaryMatchStrategy = boundaryMatchStrategySelectBox.value;
    var ontologyKey = ontologySelectBox.value;
    var files = multipleFileTestUploadInput.files;
    if(files.length === 0) {
        evaluationError.innerHTML = "Please select at least one file to evaluate.";
        evaluationError.style.display = "block";
    }
    updateEvalButton("run")
    evaluate(ontologyKey, boundaryMatchStrategy, files);
    event.preventDefault();
}, true);

