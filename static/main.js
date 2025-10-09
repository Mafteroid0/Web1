import {showNotification} from './utils.js';

document.addEventListener('DOMContentLoaded', async () => {

    let selectedX = NaN;
    let selectedY = NaN;
    let selectedR = NaN;
    const xstorage = [-4, -3, -2, -1, 0, 1, 2, 3, 4]

    loadResultsFromStorage();
    loadFormDataFromStorage();

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

    document.querySelectorAll('.XButtons input[type="radio"]').forEach(radio => {
        radio.addEventListener('change', function() {
            if (this.checked) {
                selectedX = parseFloat(this.value);
                saveFormDataToStorage();
            }
        });
    });

    const rButtons = document.querySelectorAll('.RButtons button');
    if (rButtons.length > 0) {
        rButtons.forEach(button => {
            button.addEventListener('click', function () {
                document.querySelectorAll('.RButtons button').forEach(btn => {
                    btn.style.backgroundColor = '';
                    btn.style.color = '';
                });
                this.style.backgroundColor = '#3fba16';
                selectedR = parseFloat(this.getAttribute('data-value')); // число
                document.getElementById('RChoice').value = selectedR;
                saveFormDataToStorage();
            });
        });
    }

    // строгая проверка X
    const checkX = () => {
        return new Promise((resolve, reject) => {
            const selectedRadio = document.querySelector(".XButtons input[type=radio]:checked");

            if (!selectedRadio) {
                reject("Не выбрано значение X");
                return;
            }

            const input = selectedRadio.value.trim();
            selectedX = parseFloat(input);

            if (!xstorage.includes(selectedX) || !/^(-?[0-4])(\.0+)?$/.test(input)) {
                reject("Неверное значение X");
            } else {
                resolve();
            }
        })
    }

    // строгая проверка Y
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

            if (selectedY < -5 || selectedY > 3 || isNaN(selectedY)) {
                reject("Неверное значение Y. Допустимый диапазон: от -5 до 3");
                return;
            }

            if (selectedY === 3 && !/^3(\.0+)?$/.test(yValue)) {
                reject("Y не должен превышать 3");
                return;
            }
            if (selectedY === -5 && !/^-5(\.0+)?$/.test(yValue)) {
                reject("Y не должен быть меньше -5");
                return;
            }

            resolve();
        })
    }

    // строгая проверка R
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

            if (selectedR < 1 || selectedR > 4 || isNaN(selectedR)) {
                reject("Неверное значение R. Допустимый диапазон: от 1 до 4");
                return;
            }

            if (selectedR === 4 && !/^4(\.0+)?$/.test(rValue)) {
                reject("R не должен превышать 4");
                return;
            }
            if (selectedR === 1 && !/^1(\.0+)?$/.test(rValue)) {
                reject("R не должен быть меньше 1");
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
                console.error(error)
                throw `Что-то пошло не так. Попробуйте позже. ${error}`;
            });
    }

    function saveFormDataToStorage() {
        const formData = {
            x: selectedX,
            y: document.getElementById('YChoice').value,
            r: document.getElementById('RChoice').value
        };
        localStorage.setItem('formData', JSON.stringify(formData)); // Изменено на localStorage
    }

    function loadFormDataFromStorage() {
        const savedFormData = localStorage.getItem('formData'); // Изменено на localStorage
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

        localStorage.setItem('resultsTable', JSON.stringify(resultsData)); // Изменено на localStorage
    }

    function loadResultsFromStorage() {
        const savedData = localStorage.getItem('resultsTable'); // Изменено на localStorage
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