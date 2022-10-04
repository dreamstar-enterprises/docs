---
type: Guides
category: Lambda
series: Lambda - General Purpose Library
date: 18/09/2022
toc: true
title: Array Transformation functions
description: 
layout: docs
---

# {{page.title}}
<time class="metadata" style="text-alstyleign:left"> {{page.type}} • {{page.category}} • {{page.date}}</time>

## ROTATE

### About

Rotates a 2-dimensional array anti-clockwise by 90 degrees. 

This is not the same behavior as TRANSPOSE, which reflects an array on the main diagonal from top-left to bottom-right.

#### Inputs:

  - arr - the input array
  - times - the number of times to rotate by 90 degrees
  - [iter] - optional - used as a counter by the recursion

#### More Info:

*NOTE*: 'iter' should not be used when calling this function from a spreadsheet

### Code

{% capture code %}
ROTATE = LAMBDA(arr, times, [iter],
    LET(
        _times, MOD(times, 4),
        IF(
            _times = 0,
            arr,
            LET(
                _iter, IF(ISOMITTED(iter), 1, iter),
                _cols, COLUMNS(arr),
                _rotated, INDEX(arr, SEQUENCE(1, ROWS(arr)), _cols - SEQUENCE(_cols) + 1),
                IF(_iter = _times, _rotated, ROTATE(_rotated, _times, _iter + 1))
            )
        )
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}