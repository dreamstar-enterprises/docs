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

## AFLIP

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/aflip.1182207/page-2#posts){:target="_blank"}

### About

Flips an array.

#### Inputs:

  - a - the array
  - [f] - flip argument ; 0 or omitted, flips horiz. ; 1, flips vert. ; 2, flips horiz. and vert.


### Code

{% capture code %}
AFLIP = LAMBDA(a, [f],
    LET(
        r, ROWS(a),
        c, COLUMNS(a),
        sr, SEQUENCE(r),
        sc, SEQUENCE(, c),
        x, IF(f, r - sr + 1, sr),
        y, IF(f = 1, sc, c - sc + 1),
        INDEX(IF(a = "", "", a), x, y)
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}