const display = document.getElementById("display");
const keys = ["7", "8", "9", "/", "4", "5", "6", "*", "1", "2", "3", "-", "0", "C", "=", "+"];
let left = null;
let operator = null;
let typing = false;
let wonPerDollar = null;

function calculate(a, op, b) {
  if (op === "+") return a + b;
  if (op === "-") return a - b;
  if (op === "*") return a * b;
  return b === 0 ? 0 : a / b;
}

function press(key) {
  const value = Number(display.textContent);
  if (key === "C") {
    display.textContent = "0";
    left = operator = null;
  } else if (/\d/.test(key)) {
    display.textContent = typing && display.textContent !== "0" ? display.textContent + key : key;
    typing = true;
    return;
  } else if (key === "=") {
    if (operator !== null) display.textContent = String(calculate(left, operator, value));
    left = operator = null;
  } else {
    left = operator !== null ? calculate(left, operator, value) : value;
    display.textContent = String(left);
    operator = key;
  }
  typing = false;
}

for (const key of keys) {
  const button = document.createElement("button");
  button.type = "button";
  button.textContent = key;
  button.addEventListener("click", () => press(key));
  document.getElementById("keys").append(button);
}

document.getElementById("exchange").addEventListener("click", () => {
  if (wonPerDollar) display.textContent = (Number(display.textContent) / wonPerDollar).toFixed(2);
});

fetch("__mock/rate")
  .then((response) => response.json())
  .then((rate) => {
    wonPerDollar = rate.usd;
    document.getElementById("rate").textContent = `1달러 = ${rate.usd.toLocaleString()}원 (샘플 데이터)`;
  })
  .catch(() => {
    document.getElementById("rate").textContent = "환율을 불러오지 못했어요";
  });
