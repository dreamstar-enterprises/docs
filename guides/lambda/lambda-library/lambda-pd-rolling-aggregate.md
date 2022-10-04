---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Descriptive Statistic & Basic Maths functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## PD.ROLLING.AGGREGATE

### About

Calculates a rolling aggregate of a variable where each window is of size window and the function agg is applied to each window.

#### Inputs:

  - x - a single-column numerical variable, sorted in the order the user expects to calculate rolling calculations on
  - window - an integer specifying the window length/width. For example, if window is 3, then the aggregate will be applied over the set of 3 rows ending in the current row
  - agg - a text string specifying which aggregate function should be applied over each window

#### Outputs:

  An array with ROWS(x) rows and 1 column containing the result of the aggregation over each window. For the first window-1 rows, the output array will show NA()


### Code

{% capture code %}
PD.ROLLING.AGGREGATE
= LAMBDA(x, window, agg,
    LET(
        _x, x,
        _w, window,
        _agg, agg,
        _aggs, {
            "average";
            "count";
            "counta";
            "max";
            "min";
            "product";
            "stdev.s";
            "stdev.p";
            "sum";
            "var.s";
            "var.p";
            "median";
            "mode.sngl";
            "kurt";
            "skew";
            "sem"
        },
        _thk, LAMBDA(x, LAMBDA(x)),
        _fn_aggs, MAKEARRAY(
            ROWS(_aggs),
            1,
            LAMBDA(r, c,
                CHOOSE(
                    r,
                    _thk(LAMBDA(x, AVERAGE(x))),
                    _thk(LAMBDA(x, COUNT(x))),
                    _thk(LAMBDA(x, COUNTA(x))),
                    _thk(LAMBDA(x, MAX(x))),
                    _thk(LAMBDA(x, MIN(x))),
                    _thk(LAMBDA(x, PRODUCT(x))),
                    _thk(LAMBDA(x, STDEV.S(x))),
                    _thk(LAMBDA(x, STDEV.P(x))),
                    _thk(LAMBDA(x, SUM(x))),
                    _thk(LAMBDA(x, VAR.S(x))),
                    _thk(LAMBDA(x, VAR.P(x))),
                    _thk(LAMBDA(x, MEDIAN(x))),
                    _thk(LAMBDA(x, MODE.SNGL(x))),
                    _thk(LAMBDA(x, KURT(x))),
                    _thk(LAMBDA(x, SKEW(x))),
                    _thk(LAMBDA(x, STDEV.S(x) / SQRT(_w)))
                )
            )
        ),
        _fn, XLOOKUP(_agg, _aggs, _fn_aggs),
        _i, SEQUENCE(ROWS(x)),
        _s, SCAN(
            0,
            _i,
            LAMBDA(a, b, IF(b < _w, NA(), _thk(MAKEARRAY(_w, 1, LAMBDA(r, c, INDEX(_x, b - _w + r))))))
        ),
        _out, SCAN(0, _i, LAMBDA(a, b, _fn()(INDEX(_s, b, 1)()))),
        _out
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}