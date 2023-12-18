// API Endpoint URLs
const apiUrl = 'http://localhost:8080'; // Update with your actual API base URL
const readEndpoint = '/file';



// Function to make a GET request to the API to read data
function readData() {
    // Show the textarea when the button is clicked
    document.getElementById("readResult").style.display = "block";
    // Fetch data from the API
    fetch(apiUrl + readEndpoint)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            // Update the content of the readResult textarea
            document.getElementById('readResult').innerText = data.message;
        })
        .catch(error => {
            // Handle errors
            console.error('Error reading data:', error);
            document.getElementById('readResult').innerText = 'Error reading data';
        });
}


// Function to make a POST request to the API to write data
function writeData() {
    let apikey = ''; // Use let instead of const to allow reassignment

    fetch('/config')
        .then(response => response.json())
        .then(config => {
            apikey = config.apikey;
            const writeEndpoint = '/file?app_key=' + apikey;
            const inputData = document.getElementById('writeInput').value;

            fetch(apiUrl + writeEndpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ message: inputData }),
            })
                .then(response => response.json())
                .then(data => {
                    document.getElementById('writeResult').innerText = JSON.stringify(data);
                   })
                .catch(error => {
                    console.error('Error writing data:', error);
                });
        })
        .catch(error => console.error('Error fetching configuration:', error));
}
