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

## ARWSRPT

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/repeatbynumber.1216836/){:target="_blank"}

### About

Repeats items in the first column of an array, by a 'repeat number' in the last column of the array.

#### Inputs:

  - ar - input array (repeat values should be in last column)


### Code

{% capture code %}
ARWSRPT = 
LAMBDA(ar,
    LET(
        a, DROP(ar, , -1),
        n, TAKE(ar, , -1),
        INDEX(a, TOCOL(IF(n >= SEQUENCE(, MAX(n)), SEQUENCE(ROWS(a)), NA()), 2), SEQUENCE(, COLUMNS(a)))
    )
);
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}