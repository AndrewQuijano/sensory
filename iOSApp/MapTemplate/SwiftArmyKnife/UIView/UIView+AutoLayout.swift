/*
UIView+AutoLayout.swift
Created by Peng Chia on 4/6/15.

The MIT License (MIT)

Copyright (c) 2015 William Falcon
will@hacstudios.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import UIKit

public extension UIView {
    
    /*!
        @brief Returns the width constraint of a UIView if it exists
        @return An instance of NSLayoutConstraint
    */
    func tb_widthConstraint() -> NSLayoutConstraint? {
        for item in constraints {
            if item.firstAttribute == NSLayoutConstraint.Attribute.width {
                return item
            }
        }
        
        return nil
    }
    
    /*!
        @brief Returns the height constraint of a UIView if it exists
        @return An instance of NSLayoutConstraint
    */
    func tb_heightConstraint() -> NSLayoutConstraint? {
        for item in constraints {
            if item.firstAttribute == NSLayoutConstraint.Attribute.height {
                return item
            }
        }
        
        return nil
    }
}
