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

## ASTACK

### Reference

***>>*** [Reference, Discussion, & Example Applications:](https://www.mrexcel.com/board/threads/astack.1180038/){:target="_blank"}

### About

Stacks an array for a certain width [w].

Calls [AUNSTACK](../lambda-library/lambda-aunstack.html).

### Code

{% capture code %}
ASTACK = LAMBDA(a, [w], TRANSPOSE(AUNSTACK(TRANSPOSE(a), w)));
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}