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

## EMAP

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/mp.1217166/){:target="_blank"}

### About

Lambda helper function that works with up to 4 MAP friendly array arguments.

Same syntax as a regular MAP function. Calls [MAPX](../lambda-library/lambda-mapx.html).


#### Inputs:

   - a - array 1
   - b - array 2
   - [c] - array 3
   - [d] - array 4
   - fn - helper Lambda function


### Code

{% capture code %}
EMAP = LAMBDA(a, b, [c], [d], fn,
    IF(
        TYPE(c) = 128,
        MAP(MAPX(1, a, b), MAPX(2, a, b), LAMBDA(x, y, c(x, y))),
        IF(
            TYPE(d) = 128,
            MAP(
                MAPX(1, a, b, c),
                MAPX(2, a, b, c),
                MAPX(3, a, b, c),
                LAMBDA(x, y, z, d(x, y, z))
            ),
            MAP(
                MAPX(1, a, b, c, d),
                MAPX(2, a, b, c, d),
                MAPX(3, a, b, c, d),
                MAPX(4, a, b, c, d),
                LAMBDA(x, y, z, w, fn(x, y, z, w))
            )
        )
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}