import {showNotification} from './utils.js';

document.addEventListener('DOMContentLoaded', async () => {

    let selectedX = NaN;
    let selectedY = NaN;
    let selectedR = NaN;
    let xstorage = [-4, -3, -2, -1, 0, 1, 2, 3, 4];
    let yRange = [-5, 3];
    let rRange = [1, 4];

    await loadValidationRules();
    loadResultsFromStorage();
    loadFormDataFromStorage();

    async function loadValidationRules() {
        try {
            const xResponse = await fetch('/validX', { method: 'GET' });
            if (xResponse.ok) {
                const xData = await xResponse.json();
                if (xData.validX) {
                    xstorage = xData.validX.map(x => parseFloat(x));
                    updateXButtons(xstorage);
                }
            }

            const yResponse = await fetch('/validY', { method: 'GET' });
            if (yResponse.ok) {
                const yData = await yResponse.json();
                if (yData.range) {
                    yRange = yData.range.map(y => parseFloat(y));
                    updateYInput(yRange);
                }
            }

            const rResponse = await fetch('/validR', { method: 'GET' });
            if (rResponse.ok) {
                const rData = await rResponse.json();
                if (rData.range) {
                    rRange = rData.range.map(r => parseFloat(r));
                    updateRInput(rRange);
                }
            }

        } catch (error) {
            showNotification("Используются значения по умолчанию", true);
        }
    }

    function updateXButtons(validX) {
        const xButtonsContainer = document.querySelector('.XButtons');
        if (!xButtonsContainer) {
            return;
        }

        xButtonsContainer.innerHTML = '';

        const sortedX = [...validX].sort((a, b) => a - b);

        sortedX.forEach(xValue => {
            const radioId = `x-${xValue}`;

            const radio = document.createElement('input');
            radio.type = 'radio';
            radio.id = radioId;
            radio.name = 'XChoice';
            radio.value = xValue.toString();

            const label = document.createElement('label');
            label.htmlFor = radioId;
            label.textContent = xValue.toString();

            radio.addEventListener('change', function() {
                if (this.checked) {
                    selectedX = parseFloat(this.value);
                    saveFormDataToStorage();
                }
            });

            xButtonsContainer.appendChild(radio);
            xButtonsContainer.appendChild(label);
        });


        if (!isNaN(selectedX) && validX.includes(selectedX)) {
            const radioToCheck = document.querySelector(`.XButtons input[value="${selectedX}"]`);
            if (radioToCheck) {
                radioToCheck.checked = true;
            }
        }
    }

    function updateYInput(range) {
        const yInput = document.getElementById('YChoice');
        if (!yInput) {
            return;
        }

        yInput.placeholder = `[${range[0]} ... ${range[1]}]`;
        yInput.title = `Y должен быть от ${range[0]} до ${range[1]}`;

        const currentY = parseFloat(yInput.value);
        if (!isNaN(currentY) && (currentY < range[0] || currentY > range[1])) {
            yInput.value = '';
            selectedY = NaN;
            saveFormDataToStorage();
        }

    }

    function updateRInput(range) {
        const rInput = document.getElementById('RChoice');
        if (!rInput) {
            return;
        }

        rInput.placeholder = `[${range[0]} ... ${range[1]}]`;
        rInput.title = `R должен быть от ${range[0]} до ${range[1]}`;

        const currentR = parseFloat(rInput.value);
        if (!isNaN(currentR) && (currentR < range[0] || currentR > range[1])) {
            rInput.value = '';
            selectedR = NaN;
            saveFormDataToStorage();
        }
    }

    function setupInputValidation() {
        const yInput = document.getElementById('YChoice');
        const rInput = document.getElementById('RChoice');

        const validateInput = (input) => {
            input.addEventListener('input', function () {
                this.value = this.value.replace(/[^0-9.,-]/g, '');
                this.value = this.value.replace(/,/g, '.');
                if ((this.value.match(/\./g) || []).length > 1) {
                    this.value = this.value.substring(0, this.value.lastIndexOf('.'));
                }
                if (this.value.indexOf('-') > 0) {
                    this.value = this.value.replace(/-/g, '');
                }
                if (this.value.length > 1 && this.value.includes('-')) {
                    this.value = this.value.replace(/-/g, '');
                    this.value = '-' + this.value;
                }

                saveFormDataToStorage();
            });
        };

        validateInput(yInput);
        validateInput(rInput);
    }
    setupInputValidation()

    const checkX = () => {
        return new Promise((resolve, reject) => {
            const selectedRadio = document.querySelector(".XButtons input[type=radio]:checked");

            if (!selectedRadio) {
                reject("Не выбрано значение X");
                return;
            }

            const input = selectedRadio.value.trim();
            selectedX = parseFloat(input);

            if (!xstorage.includes(selectedX)) {
                reject(`Неверное значение X. Допустимые: [${xstorage.join(', ')}]`);
            } else {
                resolve();
            }
        })
    }

    const checkY = () => {
        return new Promise((resolve, reject) => {
            const yValue = document.getElementById("YChoice").value.trim();

            if (yValue === '') {
                reject("Не введено значение Y");
                return;
            }

            if (!/^-?\d+(\.\d+)?$/.test(yValue)) {
                reject("Неверное значение Y");
                return;
            }

            selectedY = parseFloat(yValue);

            if (selectedY < yRange[0] || selectedY > yRange[1] || isNaN(selectedY)) {
                reject(`Неверное значение Y. Допустимый диапазон: от ${yRange[0]} до ${yRange[1]}`);
                return;
            }

            if (selectedY === yRange[1] && !new RegExp(`^${yRange[1]}(\\.0+)?$`).test(yValue)) {
                reject(`Y не должен превышать ${yRange[1]}`);
                return;
            }
            if (selectedY === yRange[0] && !new RegExp(`^${yRange[0]}(\\.0+)?$`).test(yValue)) {
                reject(`Y не должен быть меньше ${yRange[0]}`);
                return;
            }

            resolve();
        })
    }

    const checkR = () => {
        return new Promise((resolve, reject) => {
            const rValue = document.getElementById("RChoice").value.trim();

            if (rValue === '') {
                reject("Не введено значение R");
                return;
            }

            if (!/^-?\d+(\.\d+)?$/.test(rValue)) {
                reject("Неверное значение R");
                return;
            }

            selectedR = parseFloat(rValue);

            if (selectedR < rRange[0] || selectedR > rRange[1] || isNaN(selectedR)) {
                reject(`Неверное значение R. Допустимый диапазон: от ${rRange[0]} до ${rRange[1]}`);
                return;
            }

            if (selectedR === rRange[1] && !new RegExp(`^${rRange[1]}(\\.0+)?$`).test(rValue)) {
                reject(`R не должен превышать ${rRange[1]}`);
                return;
            }
            if (selectedR === rRange[0] && !new RegExp(`^${rRange[0]}(\\.0+)?$`).test(rValue)) {
                reject(`R не должен быть меньше ${rRange[0]}`);
                return;
            }

            resolve();
        })
    }

    document.querySelector('.submitCord').addEventListener("click", () => {
        Promise.all([
            checkX(),
            checkY(),
            checkR()
        ]).then(() => {
            return sendToServer(selectedX, selectedY, selectedR);
        }).then(() => {
            showNotification("Данные отправлены!", false);
        }).catch((error) => {
            showNotification(error, true);
        });
    });

    function sendToServer(x, y, r) {
        return fetch(`/calculate?x=${x}&y=${y}&r=${r}`, {
            method: "GET",
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                addResultToTable(data);
                return data;
            })
            .catch(error => {
                throw `Что-то пошло не так. Попробуйте позже. ${error}`;
            });
    }

    function saveFormDataToStorage() {
        const formData = {
            x: selectedX,
            y: document.getElementById('YChoice').value,
            r: document.getElementById('RChoice').value
        };
        localStorage.setItem('formData', JSON.stringify(formData));
    }

    function loadFormDataFromStorage() {
        const savedFormData = localStorage.getItem('formData');
        if (savedFormData) {
            const formData = JSON.parse(savedFormData);

            if (!isNaN(formData.x)) {
                const xRadio = document.querySelector(`.XButtons input[type="radio"][value="${formData.x}"]`);
                if (xRadio) {
                    xRadio.checked = true;
                    selectedX = formData.x;
                }
            }

            if (formData.y) {
                document.getElementById('YChoice').value = formData.y;
            }

            if (formData.r) {
                document.getElementById('RChoice').value = formData.r;
                selectedR = parseFloat(formData.r);
            }
        }
    }

    function saveResultsToStorage() {
        const tableRows = document.querySelectorAll('#results-table tbody tr');
        const resultsData = [];

        tableRows.forEach(row => {
            resultsData.push({
                x: row.cells[0].textContent,
                y: row.cells[1].textContent,
                r: row.cells[2].textContent,
                result: row.cells[3].textContent,
                workTime: row.cells[4].textContent,
                execTime: row.cells[5].textContent
            });
        });

        localStorage.setItem('resultsTable', JSON.stringify(resultsData));
    }

    function loadResultsFromStorage() {
        const savedData = localStorage.getItem('resultsTable');
        if (savedData) {
            const resultsData = JSON.parse(savedData);
            const tableBody = document.querySelector('#results-table tbody');
            tableBody.innerHTML = '';

            resultsData.forEach(data => {
                const row = document.createElement('tr');

                const xCell = document.createElement('td');
                xCell.textContent = data.x;

                const yCell = document.createElement('td');
                yCell.textContent = data.y;

                const rCell = document.createElement('td');
                rCell.textContent = data.r;

                const resultCell = document.createElement('td');
                resultCell.textContent = data.result;
                resultCell.className = data.result === 'Попал' ? 'result-hit' : 'result-miss';

                const timeCell = document.createElement('td');
                timeCell.textContent = data.workTime;

                const execTimeCell = document.createElement('td');
                execTimeCell.textContent = data.execTime;

                row.appendChild(xCell);
                row.appendChild(yCell);
                row.appendChild(rCell);
                row.appendChild(resultCell);
                row.appendChild(timeCell);
                row.appendChild(execTimeCell);

                tableBody.appendChild(row);
            });
        }
    }

    function addResultToTable(data) {
        const tableBody = document.querySelector('#results-table tbody');

        const row = document.createElement('tr');

        const xCell = document.createElement('td');
        xCell.textContent = data.x;

        const yCell = document.createElement('td');
        yCell.textContent = data.y;

        const rCell = document.createElement('td');
        rCell.textContent = data.r;

        const resultCell = document.createElement('td');
        let isHit = false;
        if (typeof data.result === 'boolean') {
            isHit = data.result;
        } else {
            isHit = String(data.result).toLowerCase() === 'true';
        }
        resultCell.textContent = isHit ? 'Попал' : 'Не попал';
        resultCell.className = isHit ? 'result-hit' : 'result-miss';

        const timeCell = document.createElement('td');
        timeCell.textContent = data.workTime;

        const execTimeCell = document.createElement('td');
        execTimeCell.textContent = data.time;

        row.appendChild(xCell);
        row.appendChild(yCell);
        row.appendChild(rCell);
        row.appendChild(resultCell);
        row.appendChild(timeCell);
        row.appendChild(execTimeCell);

        tableBody.appendChild(row);

        saveResultsToStorage();
    }

    const quackButton = document.getElementById('quackButton');
    const quackSound = document.getElementById('quackSound');

    quackButton.addEventListener('click', () => {
        quackSound.currentTime = 0;
        quackSound.play();
    });
})