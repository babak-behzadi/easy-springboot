/*
 * Copyright 2026 Babak Behzadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package easy.springboot.jpa.exception;

import easy.springboot.jpa.query.FilterOperand;

import java.util.List;
import java.util.stream.Collectors;

public class InvalidFilterOperandType extends EasyJpaException {

    public InvalidFilterOperandType(FilterOperand operand) {
        super("Invalid operand type " + operand);
    }

    public InvalidFilterOperandType(List<FilterOperand> operands, Class<?> desiredClass) {
        super("Operand(s) [%s] is/are only applicable to [%s] values".formatted(
                operands.stream().map(FilterOperand::name).collect(Collectors.joining(",")),
                desiredClass.getSimpleName()));
    }

    public InvalidFilterOperandType(List<FilterOperand> operands, List<Class<?>> desiredClasses) {
        super("Operand(s) [%s] is/are only applicable to [%s] values".formatted(
                operands.stream().map(FilterOperand::name).collect(Collectors.joining(",")),
                desiredClasses.stream().map(Class::getSimpleName).collect(Collectors.joining(","))));
    }
}
