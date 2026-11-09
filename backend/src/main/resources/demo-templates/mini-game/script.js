const field = document.getElementById("field");
const target = document.getElementById("target");
const scoreLabel = document.getElementById("score");
const timeLabel = document.getElementById("time");
const startButton = document.getElementById("start");
let settings = { seconds: 15, targetSize: 56 };
let score = 0;
let timer = null;

function scoreFor(size) {
  return Math.round(100 / size * 10);
}

function moveTarget() {
  const size = settings.targetSize;
  target.style.width = target.style.height = `${size}px`;
  target.style.left = `${Math.random() * (field.clientWidth - size)}px`;
  target.style.top = `${Math.random() * (field.clientHeight - size)}px`;
}

function finish() {
  clearInterval(timer);
  target.hidden = true;
  startButton.disabled = false;
  startButton.textContent = `다시 시작 (이번 점수 ${score})`;
}

function start() {
  let remaining = settings.seconds;
  score = 0;
  scoreLabel.textContent = "0";
  timeLabel.textContent = String(remaining);
  target.hidden = false;
  startButton.disabled = true;
  moveTarget();
  timer = setInterval(() => {
    remaining -= 1;
    timeLabel.textContent = String(remaining);
    if (remaining <= 0) finish();
  }, 1000);
}

target.addEventListener("click", () => {
  score += scoreFor(settings.targetSize);
  scoreLabel.textContent = String(score);
  moveTarget();
});
startButton.addEventListener("click", start);

fetch("__mock/settings")
  .then((response) => response.json())
  .then((loaded) => {
    settings = loaded;
    timeLabel.textContent = String(settings.seconds);
  });
