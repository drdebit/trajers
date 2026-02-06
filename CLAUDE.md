# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Trajers is the codebase for the academic paper **"Sympathy for the Noise Trader: Limitations of Learning from Price"** by Matthew D. DeAngelis (Georgia State University). It implements an agent-based market simulation in Clojure to explore the limits of investor learning from price signals.

### Research Question

The paper extends the Grossman & Stiglitz (1980) model from a single valuation parameter to multiple parameters. The core finding: markets converge on the correct **aggregate price** (sum of parameters), but investors do **not** achieve efficient estimates of **individual parameters**. Uninformed investors can only approximate a linear combination consistent with price, not the true component values. Individual parameter errors increase as the number of parameters grows.

### Implications

- Price reactions to "stale" information may reflect genuine investor learning, not noise
- Explains post-earnings-announcement drift and investor use of historical disclosures
- Questions the assumption that efficient aggregate price implies efficient component pricing
- Suggests investors need communication channels beyond price (e.g., social media, analyst reports)

### Paper & Submission History

- Submitted December 2019 to the **Journal of Accounting Research** (JOAR-2019-358) for the 55th Annual (2020) JAR Conference
- Submission materials in `JAR/`: draft, cover letter, title page, data description sheet, review
- The paper embeds all Clojure source code directly for reproducibility

### Referee Feedback (JAR Review)

The referee raised three substantive criticisms:
1. **Model clarity**: Assumptions not stated in conventional math/text; reader forced to decipher code. The framework departs significantly from GS (no information acquisition, exogenous beliefs without Bayes plausibility, single-share trading, no noise traders).
2. **Contribution novelty**: Convergence without full rationality already shown by Gode & Sunder (1993). Incomplete learning unsurprising given non-optimal agents with restricted trading rules.
3. **Multi-signal learning is known**: The impossibility of fully inverting an n-dimensional information set from a single signal is established in the rational expectations literature (Fischer & Verrecchia 2000, Feltham & Xie 1994). Referee suggested the simulation should tackle problems that *cannot* be solved theoretically (e.g., dynamic limit orders, learning from trade history).

### Related Idea Files

- **`fundamental.org`** — Idea for modeling trading profits of informed vs. uninformed investors, plus speculators who predict uninformed behavior
- **`analyst.org`** — Idea for modeling manager/analyst/investor interaction around earnings forecasts and surprises

## Build & Run Commands

This is a Leiningen project:

- **REPL**: `lein repl` (primary development workflow)
- **Run**: `lein run`
- **Test all**: `lein test`
- **Test single namespace**: `lein test trajers.core-test`
- **Build uberjar**: `lein uberjar`

## Environment Requirement

Neanderthal (linear algebra library) requires Intel MKL. The project configures `LD_LIBRARY_PATH` via `lein-with-env-vars` plugin in `project.clj` pointing to `/home/matt/intel/mkl/lib/` and related directories.

## Architecture

- **`trajers.core`** — Main namespace. Most simulation functions are commented out in `core.clj`; the canonical/complete versions live in `trajers.org` as literate-programming source blocks. Active code in `core.clj`: `sample-agent`, `write-object`, `-main`.
- **`trajers.org`** — The paper itself in Emacs org-mode with embedded Clojure code blocks (exported to LaTeX/PDF). This is the authoritative source for the simulation logic. Key functions defined here:
  - `make-investor` / `make-investor-list` — Creates informed (prior = true value) or uninformed (random normal prior) investors
  - `make-market` — Initializes a market with n-parameter security vector, starting price, and 500 investors (60% informed, 40% uninformed)
  - `order-update` — Sums buy/sell orders (+1/-1/0) from investors comparing priors to price
  - `rand-prior-update` — Uninformed investors randomly select a parameter to revise toward its inferred value from price
  - `does-not-move-toward?` — Predicate: investor updates only if price fails to move toward their prior
  - `market-update` — One trading round: record price history, update price by +/-0.01, update investor priors
  - `converge?` / `iterate-markets` — Run trading rounds until price exhibits <= 2 unique values in a 10-round window
  - `avg-prior` / `sse-avg` — Measure consensus error on individual parameters
  - Visualization via `price-plot` and `plot-snap` (Oz/Vega-Lite)
- **`trajers.ml`** — Experimental namespace using Neanderthal for matrix operations.
- **`src/java/`** — MersenneTwister and MersenneTwisterFast random number generators (configured via `:java-source-paths`).
- **`trajers.tex` / `trajers.bbl`** — LaTeX export of the paper and bibliography.
- **`plots/`** — Generated figures referenced by the paper.

## Simulation Model Parameters

- 500 investors total: 300 informed (60%), 200 uninformed (40%)
- Security value = sum of n parameters, each drawn from N(0,1) rounded to 2 decimal places
- Starting price = true value + random normal draw (so price starts near but not at true value)
- Price adjusts by +/-0.01 per round based on net order imbalance
- Uninformed investors update by averaging their current parameter estimate with its inferred value from price
- Convergence: price stable (<=2 unique values) over a rolling 10-round window
- Main experiments: vary n from 1 to 10, run 500 markets per size, measure SSE of parameter estimates

## Key Dependencies

- **Oz** (`metasoarous/oz`) — Vega/Vega-Lite visualization
- **Incanter** (`incanter-core`) — Statistical sampling (e.g., `sample-normal`)
- **Neanderthal** (`uncomplicate/neanderthal`) — High-performance linear algebra (requires MKL)
