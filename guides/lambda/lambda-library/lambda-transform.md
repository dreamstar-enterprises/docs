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

## TRANSFORM

### About

Used to transform a vector once for each transformation function in transform_fns.

e.g. = TRANSFORM(vector, FUNCS(SQRT, LN, LOG_10))

### Code

{% capture code %}
TRANSFORM = LAMBDA(vector, transform_fns, REDUCE(vector, transform_fns, LAMBDA(a, b, HSTACK(a, b(vector)))));
{% endcapture %}
{% include code.html code=code lang="excel" lang-title="M.S. Excel" %}