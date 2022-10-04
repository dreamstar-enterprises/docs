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

## PD.ROLLING.AGGREGATES

### About

Calculates rolling aggregates of a variable using an arbitrary set of window lengths and aggregate function.

#### Inputs:

  - x - a single-column numerical variable, sorted in the order the user expects to calculate rolling calculations on
  - windows - a 1-dimensional array of window lengths. A window is an integer that describes the length, in terms of array elements or spreadsheet rows, of a window over which an aggregate will be calculated. 
  
  For example, if a window is 3, then the aggregate will be applied over the set of 3 rows ending in the current row. Each element in windows maps to an element in aggs, allowing for either differing window sizes per aggregation, or different window sizes for the same aggregation
  - aggs - a 1-dimensional array of aggregate functions to be applied to each window. Each aggregate maps to an element of windows. Supported aggregates are shown in the _aggs variable in pd.rolling.aggregate. 

#### Outputs:

  An array with ROWS(x) rows and MAX(COUNT(windows),COUNTA(aggs)) columns. 
  If COUNT(windows) <> COUNTA(aggs), then the smaller array will be resized to the length of the longer array, with additional elements being filled with the right-most/bottom-most element of the original array. 
  
  For example: windows = {2,3,4}; aggs = {"sum"}
  
  aggs will be resized to three elements and each new element will be filled with the right-most element of the existing array, so:

  {"sum","sum","sum"}
  
  The net result is that the output array will have three columns, showing respectively rolling 2 sum, rolling 3 sum and rolling 4 sum of x


### Code

{% capture code %}
PD.ROLLING.AGGREGATES = LAMBDA(x, windows, aggs,
    LET(
        _tr, LAMBDA(arr, LET(x, FILTER(arr, arr <> ""), IF(ROWS(x) = 1, TRANSPOSE(x), x))),
        _a, _tr(aggs),
        _w, _tr(windows),
        _resize, ROWS(_a) <> ROWS(_w),
        _rs, LAMBDA(arr, resize_to,
            MAKEARRAY(
                resize_to,
                1,
                LAMBDA(r, c, IF(r <= ROWS(arr), INDEX(arr, r, 1), INDEX(arr, ROWS(arr), 1)))
            )
        ),
        _ms, MAX(ROWS(_a), ROWS(_w)),
        _ar, IF(_resize, _rs(_a, _ms), _a),
        _wr, IF(_resize, _rs(_w, _ms), _w),
        _out, MAKEARRAY(
            ROWS(x),
            _ms,
            LAMBDA(r, c, INDEX(pd.rolling.aggregate(x, INDEX(_wr, c, 1), INDEX(_ar, c, 1)), r, 1))
        ),
        _out
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}