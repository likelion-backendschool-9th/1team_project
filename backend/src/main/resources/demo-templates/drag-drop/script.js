const log = document.getElementById("log");
let draggedId = null;

function card(task) {
  const item = document.createElement("li");
  item.textContent = task.title;
  item.draggable = true;
  item.dataset.id = task.id;
  item.addEventListener("dragstart", () => {
    draggedId = task.id;
  });
  return item;
}

async function move(column) {
  const item = document.querySelector(`li[data-id="${draggedId}"]`);
  if (!item) return;
  column.querySelector("ul").append(item);
  const response = await fetch("__mock/tasks/move", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ id: draggedId, status: column.dataset.status }),
  });
  const result = await response.json();
  log.textContent = result.message;
}

for (const column of document.querySelectorAll(".column")) {
  column.addEventListener("dragover", (event) => {
    event.preventDefault();
    column.classList.add("over");
  });
  column.addEventListener("dragleave", () => column.classList.remove("over"));
  column.addEventListener("drop", (event) => {
    event.preventDefault();
    column.classList.remove("over");
    move(column);
  });
}

fetch("__mock/tasks")
  .then((response) => response.json())
  .then((tasks) => {
    for (const task of tasks) {
      document.querySelector(`.column[data-status="${task.status}"] ul`).append(card(task));
    }
  });
