"use client";

import { useEffect, useRef } from "react";

type Particle = {
  x: number;
  y: number;
  vx: number;
  vy: number;
  baseVx: number;
  baseVy: number;
  radius: number;
  color: string;
};

const COLORS = ["#ffd04d", "#ffb020", "#ff8a1f", "#fff4cf"];
const LINK_DISTANCE = 120;
const REPEL_DISTANCE = 120;

type Props = {
  className?: string;
};

// 샌드박스 연동 전까지 대표 데모 자리에 보여 주는 파티클 미리보기.
export function ParticleCanvas({ className }: Props) {
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    const context = canvas?.getContext("2d");
    if (!canvas || !context) return;

    const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const pointer = { x: -9999, y: -9999 };
    let width = 0;
    let height = 0;
    let frame = 0;
    let particles: Particle[] = [];

    function seed() {
      const count = Math.max(40, Math.min(110, Math.round((width * height) / 7000)));
      particles = Array.from({ length: count }, () => {
        const vx = (Math.random() - 0.5) * 0.6;
        const vy = (Math.random() - 0.5) * 0.6;
        return {
          x: Math.random() * width,
          y: Math.random() * height,
          vx,
          vy,
          baseVx: vx,
          baseVy: vy,
          radius: 1.2 + Math.random() * 2,
          color: COLORS[Math.floor(Math.random() * COLORS.length)],
        };
      });
    }

    function step() {
      for (const particle of particles) {
        const dx = particle.x - pointer.x;
        const dy = particle.y - pointer.y;
        const distance = Math.hypot(dx, dy);
        if (distance > 0 && distance < REPEL_DISTANCE) {
          const force = (1 - distance / REPEL_DISTANCE) * 0.8;
          particle.vx += (dx / distance) * force;
          particle.vy += (dy / distance) * force;
        }
        particle.vx += (particle.baseVx - particle.vx) * 0.03;
        particle.vy += (particle.baseVy - particle.vy) * 0.03;
        particle.x += particle.vx;
        particle.y += particle.vy;
        if (particle.x < 0 || particle.x > width) particle.vx *= -1;
        if (particle.y < 0 || particle.y > height) particle.vy *= -1;
        particle.x = Math.max(0, Math.min(width, particle.x));
        particle.y = Math.max(0, Math.min(height, particle.y));
      }
    }

    function draw() {
      context!.clearRect(0, 0, width, height);
      context!.lineWidth = 0.8;
      for (let i = 0; i < particles.length; i += 1) {
        for (let j = i + 1; j < particles.length; j += 1) {
          const a = particles[i];
          const b = particles[j];
          const distance = Math.hypot(a.x - b.x, a.y - b.y);
          if (distance < LINK_DISTANCE) {
            context!.strokeStyle = `rgb(255 196 60 / ${(1 - distance / LINK_DISTANCE) * 0.6})`;
            context!.beginPath();
            context!.moveTo(a.x, a.y);
            context!.lineTo(b.x, b.y);
            context!.stroke();
          }
        }
      }
      for (const particle of particles) {
        context!.shadowBlur = 10;
        context!.shadowColor = particle.color;
        context!.fillStyle = particle.color;
        context!.beginPath();
        context!.arc(particle.x, particle.y, particle.radius, 0, Math.PI * 2);
        context!.fill();
      }
      context!.shadowBlur = 0;
    }

    function loop() {
      step();
      draw();
      frame = requestAnimationFrame(loop);
    }

    function resize() {
      const rect = canvas!.getBoundingClientRect();
      const ratio = Math.min(window.devicePixelRatio || 1, 2);
      width = rect.width;
      height = rect.height;
      canvas!.width = Math.round(width * ratio);
      canvas!.height = Math.round(height * ratio);
      context!.setTransform(ratio, 0, 0, ratio, 0, 0);
      seed();
      draw();
    }

    function handlePointerMove(event: PointerEvent) {
      const rect = canvas!.getBoundingClientRect();
      pointer.x = event.clientX - rect.left;
      pointer.y = event.clientY - rect.top;
    }

    function handlePointerLeave() {
      pointer.x = -9999;
      pointer.y = -9999;
    }

    const observer = new ResizeObserver(resize);
    observer.observe(canvas);
    canvas.addEventListener("pointermove", handlePointerMove);
    canvas.addEventListener("pointerleave", handlePointerLeave);
    if (!reduceMotion) frame = requestAnimationFrame(loop);

    return () => {
      cancelAnimationFrame(frame);
      observer.disconnect();
      canvas.removeEventListener("pointermove", handlePointerMove);
      canvas.removeEventListener("pointerleave", handlePointerLeave);
    };
  }, []);

  return <canvas ref={canvasRef} className={className} aria-hidden="true" />;
}
