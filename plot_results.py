import csv
import os
from collections import defaultdict

import matplotlib.pyplot as plt

TABLES = "results/tables"
PLOTS = "results/plots"
os.makedirs(PLOTS, exist_ok=True)


def load(name):
    with open(os.path.join(TABLES, name), newline="") as f:
        return list(csv.DictReader(f))


def sweep_plot(rows, sweep, xkey, xlabel, title, filename):
    series = defaultdict(list)
    for r in rows:
        if r["sweep"] == sweep:
            series[r["algorithm"]].append((int(r[xkey]), float(r["avg_time_us"])))
    plt.figure(figsize=(8, 5))
    for algo, pts in series.items():
        pts.sort()
        plt.plot([p[0] for p in pts], [p[1] for p in pts], marker="o", label=algo)
    plt.xlabel(xlabel)
    plt.ylabel("Average execution time (µs)")
    plt.title(title)
    plt.grid(True, alpha=0.3)
    plt.legend()
    plt.tight_layout()
    plt.savefig(os.path.join(PLOTS, filename), dpi=150)
    plt.close()


scaling = load("scaling.csv")
sweep_plot(scaling, "vary_V", "V", "Number of vertices (V), E = 5V",
           "Execution time vs. number of vertices", "time_vs_vertices.png")
sweep_plot(scaling, "vary_E", "E", "Number of edges (E), V = 1000",
           "Execution time vs. number of edges", "time_vs_edges.png")

# Kruskal vs Prim on the three required graph sizes
mst = load("mst.csv")
sizes = ["Small", "Medium", "Large"]
width = 0.35
plt.figure(figsize=(7, 5))
for i, algo in enumerate(["Kruskal", "Prim"]):
    vals = [float(r["avg_time_us"]) for s in sizes for r in mst
            if r["graph"] == s and r["algorithm"] == algo]
    plt.bar([x + i * width for x in range(len(sizes))], vals, width, label=algo)
plt.xticks([x + width / 2 for x in range(len(sizes))], sizes)
plt.yscale("log")
plt.ylabel("Average execution time (µs, log scale)")
plt.title("Kruskal vs. Prim")
plt.legend()
plt.tight_layout()
plt.savefig(os.path.join(PLOTS, "kruskal_vs_prim.png"), dpi=150)
plt.close()

print("Plots saved to", PLOTS)
