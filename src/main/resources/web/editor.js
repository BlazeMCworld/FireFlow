const canvas = document.getElementById("editor");
const ctx = canvas.getContext("2d");
const camera = {
    x: 0,
    y: 0,
    scale: 100
};
let widgets = [];
let needsRedraw = true;
let fontWidthData = {
    missing_char: {
        width: 6
    },
    chars: {}
};

fetch("https://rawcdn.githack.com/Owen1212055/mc-fonts/refs/heads/main/fonts/minecraft_default.json")
    .then(res => res.json())
    .then(data => {
        fontWidthData = data;
    });

function handleResize() {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    needsRedraw = true;
}

function redraw() {
    ctx.resetTransform();
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.scale(camera.scale, camera.scale);
    ctx.translate(-camera.x, -camera.y);

    for (const widget of widgets.filter(w => w.type == "filled-rect")) {
        widget.draw(widget);
    }

    for (const widget of widgets.filter(w => w.type != "filled-rect")) {
        widget.draw(widget);
    }
}

canvas.addEventListener("wheel", (event) => {
    camera.x += event.clientX / camera.scale;
    camera.y += event.clientY / camera.scale;
    if (event.deltaY < 0) {
        camera.scale *= 1.1;
    } else if (event.deltaY > 0) {
        camera.scale /= 1.1;
    }
    camera.scale = Math.min(500, Math.max(20, camera.scale));
    camera.x -= event.clientX / camera.scale;
    camera.y -= event.clientY / camera.scale;
    needsRedraw = true;
});

canvas.addEventListener("contextmenu", (event) => {
    event.preventDefault();
});


async function addMessage(message, color) {
    let div = document.getElementById("messages");
    let msg = document.createElement("p");
    msg.textContent = message;
    msg.style.color = color;
    msg.style.maxHeight = "0px";
    msg.style.overflow = "hidden";
    msg.style.transition = "max-height 0.2s, opacity 0.2s";
    msg.style.margin = "0px";
    div.appendChild(msg);
    await new Promise(r => setTimeout(r, 200));
    msg.style.maxHeight = "5rem";
    await new Promise(r => setTimeout(r, 5000));
    msg.style.opacity = "0%";
    await new Promise(r => setTimeout(r, 200));
    msg.remove();
}

const images = new Map();

window.addEventListener("resize", handleResize);
handleResize();

if (location.hash == "" || location.hash == "#") {
    widgets.push({
        draw: () => {
            ctx.resetTransform();
            ctx.fillStyle = "red";
            ctx.font = "64px sans-serif";
            ctx.fillText("Invalid link!", 32, 96);
        }
    });
} else {
    let id = location.hash.substring(1);
    let url = location.protocol.replace("http", "ws") + "//" + location.host + "/ws/editor/" + id;

    const ws = new WebSocket(url);

    addMessage("Pending authorization from in-game player...", "yellow");

    ws.addEventListener("open", () => {
        ws.send(JSON.stringify({
            type: "init", id: parseInt(id)
        }));
    });

    ws.addEventListener("close", () => {
        widgets = [];
        widgets.push({
            draw: () => {
                ctx.resetTransform();
                ctx.fillStyle = "red";
                ctx.font = "64px sans-serif";
                ctx.fillText("Disconnected!", 32, 96);
            }
        });
        camera.x = 0;
        camera.y = 0;
        camera.scale = 100;
        needsRedraw = true;
    });

    ws.addEventListener("message", (event) => {
        let data = JSON.parse(event.data);

        if (data.type == "error") {
            const { message } = data;
            addMessage(message, "red");
            return;
        }

        if (data.type == "info") {
            const { message } = data;
            addMessage(message, "white");
            return;
        }

        if (data.type == "debug") {
            const { x, y } = data;
            let w = {
                draw: () => {
                    ctx.fillStyle = "yellow";
                    ctx.beginPath();
                    ctx.arc(-x, -y, 1 / 16, 0, 2 * Math.PI);
                    ctx.fill();
                }
            };
            widgets.push(w);
            needsRedraw = true;
            setTimeout(() => {
                let index = widgets.indexOf(w);
                if (index == -1) return;
                widgets.splice(index, 1);
                needsRedraw = true;
            }, 500);
            return;
        }

        if (data.type == "snippet") {
            navigator.clipboard.writeText(data.data)
                .then(() => {
                    addMessage("Copied snippet data to clipboard!", "green");
                })
                .catch(() => {
                    addMessage("Failed to copy snippet data to clipboard!", "red");
                });
            return;
        }

        if (data.type == "remove") {
            const { id } = data;
            let index = widgets.findIndex(widget => widget.id == id);
            if (index != -1) {
                widgets.splice(index, 1);
            }
            needsRedraw = true;
            return;
        }

        if (data.type == "text") {
            const { x, y, text, color, scaleX, scaleY, id, rotation } = data;
            let index = widgets.findIndex(widget => widget.id == id);
            if (index != -1) {
                widgets[index].x = x;
                widgets[index].y = y;
                widgets[index].scaleX = scaleX;
                widgets[index].scaleY = scaleY;
                widgets[index].text = text;
                widgets[index].color = color;
                widgets[index].rotation = rotation;
                needsRedraw = true;
            } else {
                widgets.push({
                    x, y, text, color, id, scaleX, scaleY, rotation,
                    draw: (self) => {
                        ctx.font = "0.25px sans-serif";
                        ctx.fillStyle = self.color || "white";
                        ctx.save();
                        ctx.translate(0.1 - self.x, 0.25 - self.y);
                        ctx.rotate(-self.rotation * Math.PI / 180);
                        ctx.scale(self.scaleX, self.scaleY);
                        ctx.translate(-4 / 64, -3 / 64);
                        for (let char of self.text) {
                            let charData = fontWidthData.chars[char];
                            if (!charData) charData = fontWidthData.missing_char;
                            ctx.save();
                            let m = ctx.measureText(char);
                            ctx.translate(charData.width / 80 - m.width / 2, 0);
                            ctx.fillText(char, 0, 0);
                            ctx.restore();
                            ctx.translate(charData.width / 40, 0);
                        }
                        ctx.restore();
                    }
                })
            }
            return;
        }

        if (data.type == "line") {
            const { fromX, fromY, toX, toY, color, id } = data;
            let index = widgets.findIndex(widget => widget.id == id);
            if (index != -1) {
                widgets[index].fromX = fromX;
                widgets[index].fromY = fromY;
                widgets[index].toX = toX;
                widgets[index].toY = toY;
                widgets[index].color = color;
                needsRedraw = true;
            } else {
                widgets.push({
                    fromX, fromY, toX, toY, color, id,
                    draw: (self) => {
                        ctx.lineWidth = 1 / 64;
                        ctx.strokeStyle = self.color || "white";
                        ctx.beginPath();
                        ctx.moveTo(-self.fromX, -self.fromY);
                        ctx.lineTo(-self.toX, -self.toY);
                        ctx.stroke();
                    }
                })
            }
            return;
        }

        if (data.type == "item") {
            const { x, y, id, size, item } = data;
            let index = widgets.findIndex(widget => widget.id == id);
            if (index != -1) {
                widgets[index].x = x;
                widgets[index].y = y;
                widgets[index].size = size;
                widgets[index].item = item;
                needsRedraw = true;
            } else {
                widgets.push({
                    x, y, size, item, id,
                    draw: (self) => {
                        if (!images.has(self.item)) {
                            let it = new Image();
                            it.onload = () => {
                                needsRedraw = true;
                                it.loaded = true;
                            };
                            it.src = "https://rawcdn.githack.com/Owen1212055/mc-assets/refs/heads/main/assets/" + self.item.toUpperCase() + ".png";
                            images.set(self.item, it);
                        }
                        let img = images.get(self.item);
                        if (img.loaded) {
                            ctx.drawImage(img, -self.x, -self.y, self.size, self.size);
                        }
                    }
                });
                needsRedraw = true;
            }
            return;
        }

        if (data.type == "filled-rect") {
            const { x, y, width, height, color, id } = data;
            let index = widgets.findIndex(widget => widget.id == id);
            if (index != -1) {
                widgets[index].x = x;
                widgets[index].y = y;
                widgets[index].width = width;
                widgets[index].height = height;
                widgets[index].color = color;
                needsRedraw = true;
            } else {
                widgets.push({
                    x, y, width, height, color, id, type: "filled-rect",
                    draw: (self) => {
                        ctx.fillStyle = self.color;
                        ctx.globalAlpha = 0.5;
                        ctx.fillRect(-self.x, -self.y, self.width, self.height);
                        ctx.globalAlpha = 1;
                    }
                });
                needsRedraw = true;
            }
            return;
        }
    });

    const worldMouse = {
        x: 0,
        y: 0
    };

    canvas.addEventListener("mousemove", (event) => {
        worldMouse.x = event.clientX / camera.scale + camera.x;
        worldMouse.y = event.clientY / camera.scale + camera.y;
        ws.send(JSON.stringify({
            type: "move-cursor",
            x: -worldMouse.x,
            y: -worldMouse.y
        }));
    });

    let mouseDownTime = 0;
    canvas.addEventListener("mousedown", (event) => {
        ws.send(JSON.stringify({
            type: "click",
            button: event.button
        }));
        mouseDownTime = Date.now();
    });

    canvas.addEventListener("mouseup", (event) => {
        if (Date.now() - mouseDownTime < 150) return

        ws.send(JSON.stringify({
            type: "click",
            button: event.button
        }));
    });

    canvas.addEventListener("keydown", (event) => {
        event.preventDefault();
        if (event.code == "KeyF") {
            ws.send(JSON.stringify({
                type: "key-down",
                key: event.code
            }));
        }
        if (event.code == "KeyT") {
            let panel = document.createElement("div");
            panel.style.position = "fixed";
            panel.style.top = "50%";
            panel.style.left = "50%";
            panel.style.transform = "translate(-50%, -50%)";
            panel.style.borderRadius = "10px";
            panel.style.backgroundColor = "#3339";
            panel.style.padding = "1rem";

            let text = document.createElement("p");
            text.textContent = "Text Input";
            text.style.margin = "0";
            text.style.textAlign = "center";
            panel.appendChild(text);

            let input = document.createElement("input");
            input.style.margin = "0.5rem";
            input.style.padding = "0.2rem";
            input.style.font = "2rem sans-serif";
            input.style.outline = "none";
            input.style.border = "1px solid black";
            input.style.borderRadius = "5px";
            input.style.backgroundColor = "#3334";
            input.style.color = "white";
            panel.appendChild(input);

            document.body.appendChild(panel);
            input.focus();
            input.addEventListener("keydown", (event) => {
                if (event.code == "Enter") {
                    ws.send(JSON.stringify({
                        type: "chat",
                        content: input.value
                    }));
                    document.body.removeChild(panel);
                    canvas.focus();
                }
                if (event.code == "Escape") {
                    document.body.removeChild(panel);
                    canvas.focus();
                }
            });
            input.addEventListener("blur", () => {
                document.body.removeChild(panel);
                canvas.focus();
            });
        }
        if (event.code == "KeyW" || event.code == "ArrowUp") {
            camera.y -= 20 / camera.scale;
            needsRedraw = true;
        }
        if (event.code == "KeyS" || event.code == "ArrowDown") {
            camera.y += 20 / camera.scale;
            needsRedraw = true;
        }
        if (event.code == "KeyA" || event.code == "ArrowLeft") {
            camera.x -= 20 / camera.scale;
            needsRedraw = true;
        }
        if (event.code == "KeyD" || event.code == "ArrowRight") {
            camera.x += 20 / camera.scale;
            needsRedraw = true;
        }
    });
}

function renderLoop() {
    if (!needsRedraw) return;
    needsRedraw = false;
    redraw();
}
setInterval(renderLoop, 1000 / 60);
