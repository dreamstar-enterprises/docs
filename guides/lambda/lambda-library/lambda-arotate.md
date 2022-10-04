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

## AROTATE

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/aflip.1182207/page-2#posts){:target="_blank"}

### About

Rotates an array.

Calls [AFLIP](../lambda-library/lambda-aflip.html).

#### Inputs:

  - a - the array
  - [r] - rotate argument: -1, 90º to the "left"(CCW) once; 1, 90º to the "right"(CW) once; 2, 180º (2 times 90º CCW or CW)

### Code

{% capture code %}
AROTATE = LAMBDA(a, r, SWITCH(r, -1, AFLIP(TRANSPOSE(a), 1), 1, AFLIP(TRANSPOSE(a)), 2, AFLIP(a, 2)));
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}